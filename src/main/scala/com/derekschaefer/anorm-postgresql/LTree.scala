package com.derekschaefer.anorm.postgresql


import org.postgresql.util.PGobject

import anorm._

case class LTree(
  value: Seq[String]
)

object LTree {
  implicit def rowToStringSeq: Column[LTree] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case pgo:PGobject => {
        pgo.getType match {
          case "ltree" =>  {
            val seq: Seq[String] = pgo.getValue.split('.')
            println(seq)
            println(seq.getClass)
            Right(LTree(seq))
          }
          case x => Left(TypeDoesNotMatch(x.getClass.toString))
        }
      }
      case x => Left(TypeDoesNotMatch(x.getClass.toString))
    }
  }

  implicit def stringSeqToStatement = new ToStatement[LTree] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: LTree) {

      val pgo:org.postgresql.util.PGobject = new org.postgresql.util.PGobject()
      pgo.setType("ltree");
      pgo.setValue(aValue.value.mkString("."));

      s.setObject(index, pgo)
    }
  }
}
