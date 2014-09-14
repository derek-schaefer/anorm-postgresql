package com.derekschaefer.anorm.postgresql

import anorm._
import anorm.SqlParser._

import java.sql.{Connection, DriverManager}

import org.scalatest.{FunSuite, BeforeAndAfter}

import LTree._

case class LTreeModel(id: Option[Long] = None, params: Seq[String])

object LTreeModel {

  val table = "test_ltree"

  val simple = {
    get[Option[Long]](s"$table.id") ~
      get[Seq[String]](s"$table.params") map {
      case id~params => LTreeModel(id, params)
    }
  }

  def findAll(implicit conn: Connection): Seq[LTreeModel] = {
    SQL(s"select * from $table").as(simple *)
  }

  def insert(model: LTreeModel)(implicit conn: Connection): Option[Long] = {
    SQL(s"insert into $table (params) values ({params})").on(
      'params -> model.params
    ).executeInsert()
  }

  def update(id: Long, model: LTreeModel)(implicit conn: Connection): Int = {
    SQL(s"update $table set params = {params} where id = {id}").on(
      'id -> id,
      'params -> model.params
    ).executeUpdate()
  }

}

class LTreeTest extends FunSuite with BeforeAndAfter {

  before {
    DB.withConnection { implicit conn =>
      SQL(
        """
          |create table if not exists test_ltree (
          |  id serial primary key not null,
          |  params ltree
          |);
          |""".stripMargin
      ).execute
    }
  }

  after {
    DB.withConnection { implicit conn =>
      SQL(
        """
          |drop table test_ltree;
          |""".stripMargin
      ).execute
    }
  }

  test("insert") {
    DB.withConnection { implicit conn =>
      LTreeModel.insert(LTreeModel(params = Seq("a","b")))
      assert(LTreeModel.findAll === Seq(LTreeModel(Option(1), Seq("a", "b"))))
    }
  }

  test("update") {
    DB.withConnection { implicit conn =>
      LTreeModel.insert(LTreeModel(params = Seq("a","b")))
      LTreeModel.update(1, LTreeModel(params = Seq("a", "c")))
      assert(LTreeModel.findAll === Seq(LTreeModel(Option(1),Seq("a", "c"))))
    }
  }

}
