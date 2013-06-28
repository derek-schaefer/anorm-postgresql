package com.derekschaefer.anorm.postgresql

import anorm._
import anorm.MayErr._

import scala.collection.JavaConverters._
import scala.collection.mutable.{Map => MutableMap}

object HStore {

  implicit def rowToMap: Column[Map[String, String]] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case sm: java.util.HashMap[_, _] =>
        def convert = {
          val m = sm.asInstanceOf[java.util.HashMap[String, String]]
          m.asScala.toMap
        }
        eitherToError(Right(convert)): MayErr[SqlRequestError, Map[String, String]]
      case sa: java.sql.Array =>
        def convert = {
          val a = sa.getArray.asInstanceOf[Array[Array[String]]]
          a.map(e => e(0) -> e(1)).toMap
        }
        eitherToError(Right(convert)): MayErr[SqlRequestError, Map[String, String]]
      case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ": " + value.asInstanceOf[AnyRef].getClass))
    }
  }

  implicit def mapToStatement = new ToStatement[Map[String, String]] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: Map[String, String]) {
      s.setArray(index, s.getConnection.createArrayOf("varchar", aValue.map(t => Array(t._1, t._2)).flatten.toArray))
    }
  }

}
