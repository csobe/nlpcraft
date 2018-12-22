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

package org.nlpcraft.notification

import org.nlpcraft.plugin.{NCNotificationPlugin, NCPluginManager}
import org.nlpcraft._

/**
  * Push-based notification manager.
  */
object NCNotificationManager extends NCLifecycle("Notification manager") {
    private var plugin: NCNotificationPlugin = _
    
    /**
      * Passes over to the configured notification plugin.
      *
      * @param evtName Event name.
      * @param params Optional set of named event parameters. Note that parameter values should JSON compatible.
      */
    def addEvent(evtName: String, params: (String, Any)*): Unit = {
        ensureStarted()
    
        plugin.onEvent(evtName, params: _*)
    }
    
    /**
      * 
      * @return
      */
    override def start(): NCLifecycle = {
        plugin = NCPluginManager.getNotificationPlugin
        
        super.start()
    }
}
