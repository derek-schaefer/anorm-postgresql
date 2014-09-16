package com.derekschaefer.anorm.postgresql

import anorm._
import scala.collection.JavaConverters._
import scala.util.{Try, Success, Failure}

object HStore {

  implicit def rowToMap: Column[Map[String, String]] = Column.nonNull { (value, _) =>
    value match {
      case sm: java.util.HashMap[_, _] =>
        Try {
          sm.asInstanceOf[java.util.HashMap[String, String]].asScala.toMap
        } match {
          case Success(m) => Right(m)
          case Failure(e) => Left(TypeDoesNotMatch(e.toString))
        }
      case sa: java.sql.Array =>
        Try {
          sa.getArray.asInstanceOf[Array[Array[String]]].map(e => e(0) -> e(1)).toMap
        } match {
          case Success(a) => Right(a)
          case Failure(e) => Left(TypeDoesNotMatch(e.toString))
        }
      case x => Left(TypeDoesNotMatch(x.getClass.toString))
    }
  }

  implicit def mapToStatement = new ToStatement[Map[String, String]] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: Map[String, String]) {
      s.setObject(index, aValue.asJava)
    }
  }

}
