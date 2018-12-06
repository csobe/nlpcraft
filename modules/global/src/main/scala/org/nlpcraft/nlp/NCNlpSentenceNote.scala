/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *     _   ____      ______           ______
 *    / | / / /___  / ____/________ _/ __/ /_
 *   /  |/ / / __ \/ /   / ___/ __ `/ /_/ __/
 *  / /|  / / /_/ / /___/ /  / /_/ / __/ /_
 * /_/ |_/_/ .___/\____/_/   \__,_/_/  \__/
 *        /_/
 */

package org.nlpcraft.nlp

import java.io._
import java.util.{List ⇒ JList}
import java.io.{Serializable ⇒ JSerializable}

import org.nlpcraft.ascii._
import org.nlpcraft._

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Sentence token note is a typed map of KV pairs.
  *
  * @param id Internal ID.
  */
class NCNlpSentenceNote(val id: String) extends mutable.HashMap[String/*Name*/, Serializable/*Value*/]
    with Serializable with NCAsciiLike {
    import NCNlpSentenceNote._

    // These properties should be cloned as they are auto-set when new clone
    // is created.
    private final val SKIP_CLONE = Set(
        "unid",
        "minIndex",
        "maxIndex",
        "wordIndexes",
        "wordLength",
        "tokMinIndex",
        "tokMaxIndex",
        "tokWordIndexes",
        "contiguous",
        "sparsity"
    )

    private val hash: Int = id.hashCode()

    put("unid", this.id)

    // Shortcuts for mandatory fields. (Immutable fields)
    lazy val noteType: String = get("noteType").get.asInstanceOf[String]
    lazy val tokenFrom: Int = get("tokMinIndex").get.asInstanceOf[Int] // First index.
    lazy val tokenTo: Int = get("tokMaxIndex").get.asInstanceOf[Int] // Last index.
    lazy val tokenIndexes: Seq[Int] = get("tokWordIndexes").get.asInstanceOf[JList[Int]].asScala // Includes 1st and last indices too.
    lazy val wordIndexes: Seq[Int] = get("wordIndexes").get.asInstanceOf[JList[Int]].asScala // Includes 1st and last indices too.
    lazy val sparsity: Int = get("sparsity").get.asInstanceOf[Int]
    lazy val isContiguous: Boolean = get("contiguous").get.asInstanceOf[Boolean]
    lazy val isDirect: Boolean = get("direct").get.asInstanceOf[Boolean]
    lazy val isUser: Boolean = !noteType.startsWith("nlp:")
    lazy val isSystem: Boolean = noteType.startsWith("nlp:")
    lazy val isNlp: Boolean = noteType == "nlp:nlp"

    // Typed getter.
    def data[T](key: String): T = get(key).get.asInstanceOf[T]
    def dataOpt[T](key: String): Option[T] = get(key).asInstanceOf[Option[T]]

    override def equals(obj: Any): Boolean = obj match {
        case h: NCNlpSentenceNote ⇒ h.id == id
        case _ ⇒ false
    }

    override def hashCode(): Int = hash

    /**
      * Clones this note.
      */
    def clone(indexes: Seq[Int], wordIndexes: Seq[Int], params: (String, Any)*): NCNlpSentenceNote = {
        val t = NCNlpSentenceNote(id, indexes, wordIndexes, noteType)

        t ++= this.filter(p ⇒ !SKIP_CLONE.contains(p._1))

        putAll(t, params)
    }

    /**
      *
      * @return
      */
    override def toAscii: String =
        iterator.toSeq.sortBy(_._1).foldLeft(NCAsciiTable("Key", "Value"))((t, p) ⇒ t += p).toString

    /**
      *
      * @return
      */
    override def toString(): String =
        this.toSeq.filter(_._1 != "unid").sortBy(t ⇒ { // Don't show internal ID.
            val typeSort = t._1 match {
                case "noteType" ⇒ 1
                case _ ⇒ Math.abs(t._1.hashCode)
            }
            (typeSort, t._1)
        }).map(p ⇒ s"${p._1}=${p._2}").mkString("NLP note [", ", ", "]")
}

object NCNlpSentenceNote {
    private def putAll(n: NCNlpSentenceNote, params: Seq[(String, Any)]): NCNlpSentenceNote = {
        params.foreach { case (k, v) ⇒ n.put(k, v.asInstanceOf[JSerializable]) }

        n
    }

    /**
      * Sparsity depth (or rank) as sum of all gaps in indexes. Gap is a non-consecutive index.
      *
      * @param idx Sequence of indexes.
      * @return
      */
    private def calcSparsity(idx: Seq[Int]): Int =
        idx.zipWithIndex.tail.map {
            case (v, i) ⇒ Math.abs(v - idx(i - 1))
        }.sum - idx.length + 1

    /**
      * Creates new note with given parameters.
      *
      * @param indexes Indexes in the sentence.
      * @param typ Type of the node.
      * @param params Parameters.
      */
    def apply(indexes: Seq[Int], typ: String, params: (String, Any)*): NCNlpSentenceNote = {
        apply(G.genGuid(), indexes, typ, params: _*)
    }

    /**
      * Creates new note with given parameters.
      *
      * @param id Internal ID.
      * @param indexes Indexes in the sentence.
      * @param wordIndexes Word indexes.
      * @param typ Type of the node.
      * @param params Parameters.
      */
    def apply(id: String, indexes: Seq[Int], wordIndexes: Seq[Int], typ: String, params: (String, Any)*): NCNlpSentenceNote = {
        val impl = new NCNlpSentenceNote(id)

        val sparsity = calcSparsity(wordIndexes)

        impl.put("noteType", typ)
        impl.put("tokMinIndex", indexes.min)
        impl.put("tokMaxIndex", indexes.max)
        impl.put("tokWordIndexes", indexes.asJava.asInstanceOf[JSerializable])
        impl.put("minIndex", wordIndexes.min)
        impl.put("maxIndex", wordIndexes.max)
        impl.put("wordIndexes", wordIndexes.asJava.asInstanceOf[JSerializable])
        impl.put("wordLength", wordIndexes.length)
        impl.put("sparsity", sparsity)
        impl.put("contiguous", sparsity == 0)

        putAll(impl, params)
    }

    def apply(indexes: Seq[Int], wordIndexes: Seq[Int], typ: String, params: (String, Any)*): NCNlpSentenceNote =
        apply(G.genGuid(), indexes, wordIndexes, typ, params: _*)

    /**
      * Creates new note with given parameters.
      *
      * @param id Internal ID.
      * @param indexes Indexes in the sentence.
      * @param typ Type of the note.
      * @param params Parameters.
      */
    def apply(id: String, indexes: Seq[Int], typ: String, params: (String, Any)*): NCNlpSentenceNote = {
        val impl = new NCNlpSentenceNote(id)

        val sparsity = calcSparsity(indexes)

        impl.put("noteType", typ)
        impl.put("tokMinIndex", indexes.min)
        impl.put("tokMaxIndex", indexes.max)
        impl.put("tokWordIndexes", indexes.asJava.asInstanceOf[JSerializable])
        impl.put("minIndex", impl("tokMinIndex"))
        impl.put("maxIndex", impl("tokMaxIndex"))
        impl.put("wordIndexes", impl("tokWordIndexes"))
        impl.put("wordLength",  indexes.length)
        impl.put("sparsity", sparsity)
        impl.put("contiguous", sparsity == 0)

        putAll(impl, params)
    }
}