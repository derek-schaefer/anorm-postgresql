package com.derekschaefer.anorm.postgresql

import anorm._
import org.postgresql.util.PGobject

case class LTree(
  value: Seq[String]
)

object LTree {

  implicit def rowToStringSeq: Column[LTree] = Column.nonNull { (value, _) =>
    value match {
      case pgo: PGobject =>
        pgo.getType match {
          case "ltree" => Right(LTree(pgo.getValue.split('.')))
          case x => Left(TypeDoesNotMatch(x.getClass.toString))
        }
      case x => Left(TypeDoesNotMatch(x.getClass.toString))
    }
  }

  implicit def stringSeqToStatement = new ToStatement[LTree] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: LTree) {
      val pgo = new PGobject
      pgo.setType("ltree")
      pgo.setValue(aValue.value.mkString("."))
      s.setObject(index, pgo)
    }
  }

}
