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

package org.nlpcraft.probe.mgrs.nlp.enrichers.context

import org.nlpcraft._
import org.nlpcraft.nlp._
import org.nlpcraft.probe.NCModelDecorator
import org.nlpcraft.probe.mgrs.nlp.NCProbeEnricher
import org.nlpcraft.nlp.stem._

import scala.collection.mutable

/**
 * Context-based enricher. This enricher should always be run one of the last.
 */
object NCContextEnricher extends NCProbeEnricher("PROBE context-based enricher") {
    private final val STOP_BEFORE = Seq("DT", "IN", "PRP", "PRP$", "TO", "WDT", "WP", "WP$", "WRB")

    @throws[NCE]
    private final val GEO_PRE_WORDS: Seq[Seq[String]] =
        // NOTE: stemmatisation is done already by generator.
        G.readTextResource(s"context/geo_pre_words.txt", "UTF-8", logger).toSeq.map(_.split(" ").toSeq)

    private final val GEO_KIND_STOPS =
        Map(
            "CITY" → Seq("city", "town"),
            "COUNTRY" → Seq("country", "land", "countryside", "area", "territory"),
            "REGION" → Seq("region", "area", "state", "county", "district", "ground", "territory"),
            "CONTINENT" → Seq("continent", "land", "area")
        ).map(p ⇒ p._1 → p._2.map(NCStemmerManager.stems))

    private def isStopPos(t: NCNlpSentenceToken): Boolean = STOP_BEFORE.contains(t.pos)

    // Override stopWord.
    private def mark(tok: NCNlpSentenceToken) = tok.getNlpNote += "stopWord" → true

    /**
      * Marks stop words
      *
      * @param ns Sentence.
      * @param noteType Note type.
      */
    private def markStopsBefore(ns: NCNlpSentence, noteType: String): Unit =
        markFilteredBefore(ns, noteType, (t: NCNlpSentenceToken) ⇒ t.isSimpleWord && (isStopPos(t) || t.isStopword))
    
    /**
      * Marks tokens.
      *
      * @param ns Sentence.
      * @param noteType Note type.
      * @param f Filter.
      */
    private def markFilteredBefore(ns: NCNlpSentence, noteType: String, f: NCNlpSentenceToken ⇒ Boolean): Unit =
        for (n ← ns.getNotes(noteType))
            if (n.tokenFrom > 0)
                ns.
                    take(n.tokenFrom).
                    reverse.
                    takeWhile(f).
                    filter(!_.isStopword).
                    foreach(mark)
    
    /**
      * Marks words before candidates.
      *
      * @param ns Sentence.
      * @param n Note.
      * @param candidates Candidates.
      */
    private def markWordsBefore(ns: NCNlpSentence, n: NCNlpSentenceNote, candidates: Seq[Seq[String]]) {
        val ts = ns.take(n.tokenFrom).reverse.takeWhile(t ⇒ t.isStopword || t.isSimpleWord).filter(!_.isStopword).reverse

        if (ts.nonEmpty) {
            val stems = ts.map(_.stem)

            candidates.find(cds ⇒ stems.endsWith(cds)) match {
                case Some(cd) ⇒ ts.drop(ts.size - cd.size).filter(!_.isStopword).foreach(mark)
                case None ⇒ // No-op.
            }
        }
    }
    
    /**
      * Processes geo tokens.
      *
      * @param ns Sentence.
      */
    private def processGeo(ns: NCNlpSentence): Unit = {
        // 1. Marks some words before GEO.
        for (n ← ns.getNotes("nlp:geo"))
            markWordsBefore(ns, n, GEO_PRE_WORDS)

        // 2. Marks stop-words like prepositions before.
        markStopsBefore(ns, "nlp:geo")

        // 3. Marks stop word like city, town etc for corresponding geo.
        for (n ← ns.getNotes("nlp:geo"))
            GEO_KIND_STOPS.get(n.data[String]("kind")) match {
                case Some(stops) ⇒ markGeoKindStops(ns, n, stops)
                case None ⇒ // No-op.
            }
    }

    /**
      * Finds and marks as stop words GEO kind dependent words.
      *
      * @param ns Sentence.
      * @param geoNote GEO note.
      * @param stops Stop words.
      */
    private def markGeoKindStops(ns: NCNlpSentence, geoNote: NCNlpSentenceNote, stops: Seq[String]): Unit = {
        def process(toks: Seq[NCNlpSentenceToken]): Unit =
            toks.find(!_.isStopword) match {
                case Some(t) ⇒ if (stops.contains(t.stem)) mark(t)
                case None ⇒ // No-op.
            }

        process(ns.filter(_.index > geoNote.tokenTo))
        process(ns.filter(_.index < geoNote.tokenFrom).reverse)
    }
    
    /**
      * Processes dates.
      *
      * @param ns Sentence.
      */
    private def processDate(ns: NCNlpSentence): Unit = {
        markStopsBefore(ns, "nlp:date")
        markFilteredBefore(ns, "nlp:date", (t: NCNlpSentenceToken) ⇒ t.isSimpleWord && t.pos == "TO" || t.isStopword)
    }

    /**
      * Gets flag.
      *
      * @param t Token.
      */
    private def isNumericUserElement(t: NCNlpSentenceToken): Boolean = t.exists(n ⇒
        if (n.isUser)
            n.get("dataType") match {
                case Some(v) ⇒ v == "LONG" || v == "DOUBLE"
                case None ⇒ false
            }
        else
            false
    )

    /**
      * Gets `is-numeric` processing flag.
      *
      * Sequence: first user token, after numeric condition, any tokens between.
      *
      * It is because complex sentences like: 'x:num' which located more than 10 km
      * (we can't configure all words like `located`)
      *
      * @param toks Tokens.
      */
    private def isNumeric(toks: Seq[NCNlpSentenceToken]): Boolean =
        if (toks.size > 1 && isNumericUserElement(toks.head)) {
            val others = toks.drop(1).dropWhile(_.isStopword)

            others.size match {
                case 0 ⇒ false
                case _ ⇒
                    val numTokCandidate = others.last

                    if (numTokCandidate.exists(_.noteType == "nlp:num")) {
                        // Single index can be used, because user token already collapsed as one.
                        numTokCandidate.getNotes("nlp:num").head += "index" → toks.head.index

                        true
                    }
                    else
                        false
            }
        }
        else
            false

    /**
      * Processes num tokens.
      *
      * @param ns Sentence.
      */
    private def processNumerics(ns: NCNlpSentence): Unit = {
        val buf = mutable.Buffer.empty[Set[NCNlpSentenceToken]]

        def areSuitableTokens(toks: Seq[NCNlpSentenceToken]): Boolean =
            toks.forall(t ⇒ !t.isQuoted && !t.isBracketed) && !buf.exists(_.exists(t ⇒ toks.contains(t)))

        for (toks ← ns.tokenMixWithStopWords().sortBy(p ⇒ (p.size, -p.head.index)) if areSuitableTokens(toks))
            if (isNumeric(toks))
                buf += toks.toSet
    }

    /**
      *
      * @param mdl Model decorator.
      * @param ns NLP sentence to enrich.
      */
    @throws[NCE]
    override def enrich(mdl: NCModelDecorator, ns: NCNlpSentence) {
        // This stage must not be 1st enrichment stage.
        assume(ns.nonEmpty)

        processGeo(ns)
        processDate(ns)
        processNumerics(ns)
    }
}