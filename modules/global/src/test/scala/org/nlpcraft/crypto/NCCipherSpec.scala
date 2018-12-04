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

package org.nlpcraft.crypto

import java.util.Base64
import org.nlpcraft._
import org.scalatest.{BeforeAndAfter, FlatSpec}

/**
 * Tests for crypto and PKI support.
 */
class NCCipherSpec extends FlatSpec with BeforeAndAfter {
    behavior of "Cipher"

    private final val IN = "abcdefghijklmnopqrstuvwxyz0123456789"

    it should "properly encrypt and decrypt" in {
        val s1 = NCCipher.encrypt(IN)
        val s2 = NCCipher.decrypt(s1)

        assertResult(IN)(s2)
    }

    it should "produce different encrypted string for the same input" in {
        val s1 = NCCipher.encrypt(IN)
        val s2 = NCCipher.encrypt(IN)
        val s3 = NCCipher.encrypt(IN)

        assert(s1 != s2)
        assert(s2 != s3)

        val r1 = NCCipher.decrypt(s1)
        val r2 = NCCipher.decrypt(s2)
        val r3 = NCCipher.decrypt(s3)

        assertResult(r1)(IN)
        assertResult(r2)(IN)
        assertResult(r3)(IN)
    }
    
    it should "properly encrypt" in {
        val buf = new StringBuilder
        
        // Max long string.
        for (i ← 0 to 1275535) buf.append(i.toString)
        
        val str = buf.toString
        
        val bytes = G.serialize(str)
        
        val key = NCCipher.makeTokenKey(G.genGuid())
        
        val now = System.currentTimeMillis()
        
        val sec = NCCipher.encrypt(Base64.getEncoder.encodeToString(bytes), key)
        
        val dur = System.currentTimeMillis() - now
        
        println(s"Input length: ${str.length}")
        println(s"Output length: ${sec.length}")
        println(s"Total: $dur ms.")
    }
}
