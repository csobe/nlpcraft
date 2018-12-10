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

package org.nlpcraft2.nlp.enrichers.date

import java.text.SimpleDateFormat
import java.time._

object NCDateRange {
    private final val UTC = ZoneId.of("UTC")
    private final val FMT = new SimpleDateFormat("MM-dd-yyyy")

    val MAX_VALUE: Long = Long.MaxValue
    val MIN_VALUE: Long = FMT.parse("01-01-0001").getTime

    /**
      * Constructors for date range.
      *
      * @param from
      * @param to
      * @param text
      * @param periods
      * @return
      */
    def apply(from: Long, to: Long, text: String, periods: Seq[String]) =
        new NCDateRange(from, to, text, periods)

    /**
      *
      * @param from
      * @param to
      * @return
      */
    def apply(from: Long, to: Long) = new NCDateRange(from, to, "?", Seq.empty)

    /**
      *
      * @param l
      * @param limit
      * @param dflt
      * @return
      */
    private def long2String(l: Long, limit: Long, dflt: String): String =
        if (l == limit) dflt else FMT.format(new java.util.Date(l))
}

import NCDateRange._

/**
  * Date range data holder.
  */
class NCDateRange(val from: Long, val to: Long, val text: String, val periods: Seq[String]) {
    private val hash = 31 * from.hashCode() + to.hashCode()

    /**
      *
      * @return
      */
    def isComplex: Boolean = periods.contains(":")

    /**
      *
      * @return
      */
    def isAggregator: Boolean = text == "?"

    /**
      *
      * @return
      */
    def isFromNegativeInfinity: Boolean = from == MIN_VALUE

    /**
      *
      * @return
      */
    def isToPositiveInfinity: Boolean = to == MAX_VALUE

    /**
      * Creates date range with inclusive upper bound.
      *
      * @return
      */
    def mkInclusiveDateRange: NCDateRange = {
        var dt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(to), UTC)

        if (dt.getHour == 0 && dt.getMinute == 0 && dt.getSecond == 0)
            dt = dt.minusSeconds(1)

        new NCDateRange(from, Instant.from(dt).toEpochMilli, text, periods)
    }

    /**
      * Gets `include` flag.
      *
      * @param range Range.
      */
    def include(range: NCDateRange): Boolean = from <= range.from && to >= range.to

    /**
      * Gets length in milliseconds.
      */
    def length: Long = to - from

    override def equals(other: Any): Boolean = other match {
        case it: NCDateRange ⇒ from == it.from && to == it.to
        case _ ⇒ false
    }
    
    override def hashCode(): Int = hash
    
    override def toString: String =
        s"[${long2String(from, MIN_VALUE, "MIN_VALUE")}:${long2String(to, MAX_VALUE, "MAX_VALUE")}]"
}

