package com.derekschaefer.anorm.postgresql


import org.postgresql.util.PGobject

import anorm._

object LTree {
  implicit def rowToStringSeq: Column[Seq[String]] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case pgo:PGobject => { 
//        println(pgo); 
        println(pgo.getValue())
        println(pgo.getValue().getClass())
        val seq = pgo.getValue().split('.')

        Right(seq.toSeq) 
      }
      case x => Left(TypeDoesNotMatch(x.getClass.toString))
    }
  }

  implicit def stringSeqToStatement = new ToStatement[Seq[String]] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: Seq[String]) {
      val stringRepresentation = aValue.mkString(".")

      val pgo:org.postgresql.util.PGobject = new org.postgresql.util.PGobject()
      pgo.setType("ltree");
      pgo.setValue( stringRepresentation );

      s.setObject(index, pgo)
    }
  }
}
