package com.derekschaefer.anorm.postgresql

import anorm._
import anorm.SqlParser._

import org.joda.time._

import java.sql.Connection

import org.scalatest.{FunSuite, BeforeAndAfter}

import JodaTime._

case class LocalTimeModel(id: Pk[Long] = NotAssigned, time: LocalTime)

object LocalTimeModel {

  val table = "test_localtime"

  val simple = {
    get[Pk[Long]](s"$table.id") ~
    get[LocalTime](s"$table.time") map {
      case id~time => LocalTimeModel(id, time)
    }
  }

  def findAll(implicit conn: Connection): Seq[LocalTimeModel] = {
    SQL(s"select * from $table").as(simple *)
  }

  def insert(model: LocalTimeModel)(implicit conn: Connection): Option[Long] = {
    SQL(s"insert into $table (time) values ({time})").on(
      'time -> model.time
    ).executeInsert()
  }

  def update(id: Long, model: LocalTimeModel)(implicit conn: Connection): Int = {
    SQL(s"update $table set time = {time} where id = {id}").on(
      'id -> id,
      'time -> model.time
    ).executeUpdate()
  }

}

class LocalTimeTest extends FunSuite with BeforeAndAfter {

  before {
    DB.withConnection { implicit conn =>
      SQL(
        """
        |create table if not exists test_localtime (
        |  id serial primary key not null,
        |  time time
        |)""".stripMargin
      ).execute
    }
  }

  after {
    DB.withConnection { implicit conn =>
      SQL("drop table test_localtime").execute
    }
  }

  test("insert") {
    DB.withConnection { implicit conn =>
      val time = new LocalTime
      LocalTimeModel.insert(LocalTimeModel(time = time))
      assert(LocalTimeModel.findAll === Seq(LocalTimeModel(Id(1), time)))
    }
  }

  test("update") {
    DB.withConnection { implicit conn =>
      LocalTimeModel.insert(LocalTimeModel(time = new LocalTime))
      val time = new LocalTime
      LocalTimeModel.update(1, LocalTimeModel(time = time))
      assert(LocalTimeModel.findAll === Seq(LocalTimeModel(Id(1), time)))
    }
  }

}
