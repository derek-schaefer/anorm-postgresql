package com.derekschaefer.anorm.postgresql

import anorm._
import anorm.SqlParser._

import java.sql.{Connection, DriverManager}

import org.scalatest.{FunSuite, BeforeAndAfter}

import HStore._

case class TestModel(id: Option[Long] = None, params: Map[String, String])

object TestModel {

  val table = "test_hstore"

  val simple = {
    get[Option[Long]](s"$table.id") ~
    get[Map[String, String]](s"$table.params") map {
      case id~params => TestModel(id, params)
    }
  }

  def findAll(implicit conn: Connection): Seq[TestModel] = {
    SQL(s"select * from $table").as(simple *)
  }

  def hasParam(key: String)(implicit conn: Connection): Seq[TestModel] = {
    SQL(s"select * from $table where exist_inline(params, {key})").on('key -> key).as(simple *)
  }

  def insert(model: TestModel)(implicit conn: Connection): Option[Long] = {
    SQL(s"insert into $table (params) values (hstore({params}))").on(
      'params -> model.params
    ).executeInsert()
  }

  def update(id: Long, model: TestModel)(implicit conn: Connection): Int = {
    SQL(s"update $table set params = hstore({params}) where id = {id}").on(
      'id -> id,
      'params -> model.params
    ).executeUpdate()
  }

}

class HStoreTest extends FunSuite with BeforeAndAfter {

  before {
    DB.withConnection { implicit conn =>
      SQL(
        """
        |create table if not exists test_hstore (
        |  id serial primary key not null,
        |  params hstore
        |);
        |create index params_idx on test_hstore using btree(params);
        |create index params_gin_idx on test_hstore using gin(params);
        |create or replace function exist_inline(hstore, text) returns bool as $$ select $1 ? $2; $$ language sql;
        |""".stripMargin
      ).execute
    }
  }

  after {
    DB.withConnection { implicit conn =>
      SQL(
        """
        |drop table test_hstore;
        |drop function exist_inline(hstore, text);
        |""".stripMargin
      ).execute
    }
  }

  test("insert") {
    DB.withConnection { implicit conn =>
      TestModel.insert(TestModel(params = Map("asdf" -> "123")))
      assert(TestModel.findAll === Seq(TestModel(Option(1), Map("asdf" -> "123"))))
    }
  }

  test("update") {
    DB.withConnection { implicit conn =>
      TestModel.insert(TestModel(params = Map("asdf" -> "123")))
      TestModel.update(1, TestModel(params = Map("asdf" -> "42")))
      assert(TestModel.findAll === Seq(TestModel(Option(1), Map("asdf" -> "42"))))

      TestModel.update(1, TestModel(params = Map("asdf" -> "asdf", "stuff" -> "things")))
      assert(TestModel.findAll === Seq(TestModel(Option(1), Map("asdf" -> "asdf", "stuff" -> "things"))))
      TestModel.update(1, TestModel(params = Map("asdf" -> "123")))
      assert(TestModel.findAll === Seq(TestModel(Option(1), Map("asdf" -> "123"))))
    }
  }

  test("hasParam") {
    DB.withConnection { implicit conn =>
      TestModel.insert(TestModel(params = Map("asdf" -> "123")))
      TestModel.insert(TestModel(params = Map("stuff" -> "things")))
      assert(TestModel.hasParam("asdf") === Seq(TestModel(Option(1), Map("asdf" -> "123"))))
    }
  }

}
