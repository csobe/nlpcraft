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

package org.nlpcraft2.cache

import org.nlpcraft.ascii.NCAsciiLike
import org.nlpcraft2.cache.NCCacheKeyType.NCCacheKeyType
import org.nlpcraft2.json.{NCJson, NCJsonLike}

/**
 * Cache result.
 */
trait NCCacheResult extends NCJsonLike with NCAsciiLike {
    /**
     * Key type.
     */
    def keyType: NCCacheKeyType

    /**
     * Sorted flag.
     */
    def sorted: Boolean

    /**
     * Json cache result.
     */
    def json: NCJson

    /**
     * Json cache result identifier.
     */
    def mainCacheId: Long
}