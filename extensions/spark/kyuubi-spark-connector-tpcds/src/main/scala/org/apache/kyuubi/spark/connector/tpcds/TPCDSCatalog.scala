/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kyuubi.spark.connector.tpcds

import java.util

import scala.collection.JavaConverters._

import org.apache.spark.sql.catalyst.analysis.{NoSuchNamespaceException, NoSuchTableException}
import org.apache.spark.sql.connector.catalog.{Identifier, NamespaceChange, SupportsNamespaces, Table => SparkTable, TableCatalog, TableChange}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

class TPCDSCatalog extends TableCatalog with SupportsNamespaces {

  val tables: Array[String] = TPCDSSchemaUtils.BASE_TABLES.map(_.getName)

  val scales: Array[Int] = TPCDSStatisticsUtils.SCALES

  val databases: Array[String] = scales.map("sf" + _)

  var options: CaseInsensitiveStringMap = _

  var _name: String = _

  override def name: String = _name

  override def initialize(name: String, options: CaseInsensitiveStringMap): Unit = {
    this.options = options
    this._name = name
  }

  override def listTables(namespace: Array[String]): Array[Identifier] = namespace match {
    case Array(db) if databases contains db => tables.map(Identifier.of(namespace, _))
    case _ => throw new NoSuchNamespaceException(namespace.mkString("."))
  }

  override def loadTable(ident: Identifier): SparkTable = (ident.namespace, ident.name) match {
    case (Array(db), table) if (databases contains db) && tables.contains(table.toLowerCase) =>
      new TPCDSTable(table.toLowerCase, scales(databases indexOf db), options)
    case (_, _) => throw new NoSuchTableException(ident)
  }

  override def createTable(
      ident: Identifier,
      schema: StructType,
      partitions: Array[Transform],
      properties: util.Map[String, String]): SparkTable =
    throw new UnsupportedOperationException

  override def alterTable(ident: Identifier, changes: TableChange*): SparkTable =
    throw new UnsupportedOperationException

  override def dropTable(ident: Identifier): Boolean =
    throw new UnsupportedOperationException

  override def renameTable(oldIdent: Identifier, newIdent: Identifier): Unit =
    throw new UnsupportedOperationException

  override def listNamespaces(): Array[Array[String]] = databases.map(Array(_))

  override def listNamespaces(namespace: Array[String]): Array[Array[String]] = namespace match {
    case Array() => listNamespaces()
    case Array(db) if databases contains db => Array.empty
    case _ => throw new NoSuchNamespaceException(namespace)
  }

  override def loadNamespaceMetadata(namespace: Array[String]): util.Map[String, String] =
    namespace match {
      case Array(_) => Map.empty[String, String].asJava
      case _ => throw new NoSuchNamespaceException(namespace)
    }

  override def createNamespace(namespace: Array[String], metadata: util.Map[String, String]): Unit =
    throw new UnsupportedOperationException

  override def alterNamespace(namespace: Array[String], changes: NamespaceChange*): Unit =
    throw new UnsupportedOperationException

  // Removed in SPARK-37929
  def dropNamespace(namespace: Array[String]): Boolean =
    throw new UnsupportedOperationException

  // Introduced in SPARK-37929
  def dropNamespace(namespace: Array[String], cascade: Boolean): Boolean =
    throw new UnsupportedOperationException
}
