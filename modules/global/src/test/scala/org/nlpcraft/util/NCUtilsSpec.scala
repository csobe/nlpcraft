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

package org.nlpcraft.util

import org.nlpcraft._
import org.scalatest.FlatSpec

/**
 * Utilities tests.
 */
class NCUtilsSpec extends FlatSpec {
    "inflate() and deflate() methods" should "work" in {
        val rawStr = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. " +
            "Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when " +
            "an unknown printer took a galley of type and scrambled it to make a type specimen book. " +
            "It has survived not only five centuries, but also the leap into electronic typesetting, " +
            "remaining essentially unchanged. It was popularised in the 1960s with the release of " +
            "Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing " +
            "software like Aldus PageMaker including versions of Lorem Ipsum."
        
        println(s"Original length: " + rawStr.length())
        
        val zipStr = G.compress(rawStr)
        val rawStr2 = G.uncompress(zipStr)
        
        println(s"Compressed length: " + zipStr.length())
        
        assert(rawStr == rawStr2)
    }
    
    "toFirstLastName() method" should "properly work" in {
        assert(G.toFirstLastName("A BbbBB") == ("A", "Bbbbb"))
        assert(G.toFirstLastName("aBC BbbBB CCC") == ("Abc", "Bbbbb ccc"))
        assert(G.toFirstLastName("abc b C C C") == ("Abc", "B c c c"))
    }

    "sleep method" should "work without unnecessary logging" in {
        val t = new Thread() {
            override def run(): Unit = {
                while (!isInterrupted) {
                    println("before sleep")

                    G.sleep(100)

                    println("after sleep")
                }
            }
        }

        t.start()

        G.sleep(550)

        t.interrupt()

        t.join()

        println("OK")
    }
}