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

package org.nlpcraft.server.cache

import org.apache.ignite.IgniteException
import org.apache.ignite.lang.IgniteBiInClosure
import org.nlpcraft.server.db.NCDbManager
import org.nlpcraft.server.db.postgres.NCPsql
import org.nlpcraft.server.ignite.NCIgniteCacheStore
import org.nlpcraft.server.mdo.NCDataSourceMdo

import scala.util.control.Exception._

/**
 * Data source cache storage.
 */
class NcDsCacheStore extends NCIgniteCacheStore[Long, NCDataSourceMdo] {
    @throws[IgniteException]
    override protected def put(id: Long, ds: NCDataSourceMdo): Unit =
        catching(wrapNCE) {
            NCPsql.sql {
                val updated = NCDbManager.updateDataSource(ds.id, ds.name, ds.shortDesc)

                if (updated == 0)
                    NCDbManager.addDataSource(
                        ds.id,
                        ds.name,
                        ds.shortDesc,
                        ds.modelId,
                        ds.modelName,
                        ds.modelVersion,
                        ds.modelConfig,
                        ds.isTemporary
                    )
            }
        }

    @throws[IgniteException]
    override protected def get(id: Long): NCDataSourceMdo =
        catching(wrapNCE) {
            NCPsql.sql {
                NCDbManager.getDataSource(id)
            }.orNull
        }

    @throws[IgniteException]
    override protected def remove(id: Long): Unit =
        catching(wrapNCE) {
            NCPsql.sql {
                NCDbManager.getDataSource(id) match {
                    case Some(ds) ⇒
                        if (ds.isTemporary)
                            NCDbManager.eraseDataSource(id)
                        else
                            NCDbManager.deleteDataSource(id)

                    case None ⇒ // No-op.
                }
            }
        }

    @throws[IgniteException]
    override def loadCache(clo: IgniteBiInClosure[Long, NCDataSourceMdo], args: AnyRef*): Unit =
        catching(wrapNCE) {
            NCPsql.sql {
                val items =
                    args.size match {
                        case 0 ⇒ NCDbManager.getAllDataSources
                        case _ ⇒ args.map(_.asInstanceOf[Long]).flatMap(NCDbManager.getDataSource)
                    }

                items.foreach(item ⇒ clo.apply(item.id, item))
            }
        }
}