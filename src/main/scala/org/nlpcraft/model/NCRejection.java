/*
 * “Commons Clause” License, https://commonsclause.com/
 *
 * The Software is provided to you by the Licensor under the License,
 * as defined below, subject to the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights
 * under the License will not include, and the License does not grant to
 * you, the right to Sell the Software.
 *
 * For purposes of the foregoing, “Sell” means practicing any or all of
 * the rights granted to you under the License to provide to third parties,
 * for a fee or other consideration (including without limitation fees for
 * hosting or consulting/support services related to the Software), a
 * product or service whose value derives, entirely or substantially, from
 * the functionality of the Software. Any license notice or attribution
 * required by the License must also include this Commons Clause License
 * Condition notice.
 *
 * Software:    NLPCraft
 * License:     Apache 2.0, https://www.apache.org/licenses/LICENSE-2.0
 * Licensor:    Copyright (C) 2018 DataLingvo, Inc. https://www.datalingvo.com
 *
 *     _   ____      ______           ______
 *    / | / / /___  / ____/________ _/ __/ /_
 *   /  |/ / / __ \/ /   / ___/ __ `/ /_/ __/
 *  / /|  / / /_/ / /___/ /  / /_/ / __/ /_
 * /_/ |_/_/ .___/\____/_/   \__,_/_/  \__/
 *        /_/
 */

package org.nlpcraft.model;

/**
 * When thrown indicates that user input cannot be processed as is.
 * <p>
 * This exception typically indicates that user has not provided enough information in the input string
 * to have it processed automatically. In most cases this means that the user's input is either too short
 * or too simple, too long or too complex, missing required context, or unrelated to selected data source.
 *
 * @see NCModel#query(NCQueryContext)
 */
public class NCRejection extends RuntimeException {
    /**
     * Creates new rejection exception with given message.
     *
     * @param msg Rejection message.
     */
    public NCRejection(String msg) {
        super(msg);
    }

    /**
     * Creates new rejection exception with given message and cause.
     *
     * @param msg Rejection message. 
     * @param cause Cause of this exception.
     */
    public NCRejection(String msg, Throwable cause) {
        super(msg, cause);
    }
}
