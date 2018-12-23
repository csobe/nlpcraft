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

package org.nlpcraft.plugin

import java.security.Key

import org.nlpcraft.NCE

/**
  * Probe manager authentication manager.
  */
trait NCProbeAuthenticationPlugin extends NCPlugin {
    /**
      * Given probe token hash this method should return an encryption key for that probe token.
      * Encryption key will be used for secure communication between server and the probe.
      * 
      * @param probeTokenHash Probe token hash.
      * @return An encryption key for a given probe token hash, or `None` if given hash is unknown or invalid.
      */
    @throws[NCE]
    def acquireKey(probeTokenHash: String): Option[Key]
}