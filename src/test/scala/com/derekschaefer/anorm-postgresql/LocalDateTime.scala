package com.derekschaefer.anorm.postgresql

import anorm._
import anorm.SqlParser._

import org.joda.time._

import java.sql.Connection

import org.scalatest.{FunSuite, BeforeAndAfter}

import JodaTime._

case class LocalDateTimeModel(id: Pk[Long] = NotAssigned, time: LocalDateTime)

object LocalDateTimeModel {

  val table = "test_localdatetime"

  val simple = {
    get[Pk[Long]](s"$table.id") ~
    get[LocalDateTime](s"$table.time") map {
      case id~time => LocalDateTimeModel(id, time)
    }
  }

  def findAll(implicit conn: Connection): Seq[LocalDateTimeModel] = {
    SQL(s"select * from $table").as(simple *)
  }

  def insert(model: LocalDateTimeModel)(implicit conn: Connection): Option[Long] = {
    SQL(s"insert into $table (time) values ({time})").on(
      'time -> model.time
    ).executeInsert()
  }

  def update(id: Long, model: LocalDateTimeModel)(implicit conn: Connection): Int = {
    SQL(s"update $table set time = {time} where id = {id}").on(
      'id -> id,
      'time -> model.time
    ).executeUpdate()
  }

}

class LocalDateTimeTest extends FunSuite with BeforeAndAfter {

  before {
    DB.withConnection { implicit conn =>
      SQL(
        """
        |create table if not exists test_localdatetime (
        |  id serial primary key not null,
        |  time timestamp
        |)""".stripMargin
      ).execute
    }
  }

  after {
    DB.withConnection { implicit conn =>
      SQL("drop table test_localdatetime").execute
    }
  }

  test("insert") {
    DB.withConnection { implicit conn =>
      val time = new LocalDateTime
      LocalDateTimeModel.insert(LocalDateTimeModel(time = time))
      assert(LocalDateTimeModel.findAll === Seq(LocalDateTimeModel(Id(1), time)))
    }
  }

  test("update") {
    DB.withConnection { implicit conn =>
      LocalDateTimeModel.insert(LocalDateTimeModel(time = new LocalDateTime))
      val time = new LocalDateTime
      LocalDateTimeModel.update(1, LocalDateTimeModel(time = time))
      assert(LocalDateTimeModel.findAll === Seq(LocalDateTimeModel(Id(1), time)))
    }
  }

}
