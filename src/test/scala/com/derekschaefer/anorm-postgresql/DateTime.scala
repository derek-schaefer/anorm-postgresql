package com.derekschaefer.anorm.postgresql

import anorm._
import anorm.SqlParser._

import org.joda.time._

import java.sql.Connection

import org.scalatest.{FunSuite, BeforeAndAfter}

import JodaTime._

case class DateTimeModel(id: Pk[Long] = NotAssigned, time: DateTime)

object DateTimeModel {

  val table = "test_datetime"

  val simple = {
    get[Pk[Long]](s"$table.id") ~
    get[DateTime](s"$table.time") map {
      case id~time => DateTimeModel(id, time)
    }
  }

  def findAll(implicit conn: Connection): Seq[DateTimeModel] = {
    SQL(s"select * from $table").as(simple *)
  }

  def insert(model: DateTimeModel)(implicit conn: Connection): Option[Long] = {
    SQL(s"insert into $table (time) values ({time})").on(
      'time -> model.time
    ).executeInsert()
  }

  def update(id: Long, model: DateTimeModel)(implicit conn: Connection): Int = {
    SQL(s"update $table set time = {time} where id = {id}").on(
      'id -> id,
      'time -> model.time
    ).executeUpdate()
  }

}

class DateTimeTest extends FunSuite with BeforeAndAfter {

  before {
    DB.withConnection { implicit conn =>
      SQL(
        """
        |create table if not exists test_datetime (
        |  id serial primary key not null,
        |  time timestamp with time zone
        |)""".stripMargin
      ).execute
    }
  }

  after {
    DB.withConnection { implicit conn =>
      SQL("drop table test_datetime").execute
    }
  }

  test("insert") {
    DB.withConnection { implicit conn =>
      val time = new DateTime
      DateTimeModel.insert(DateTimeModel(time = time))
      assert(DateTimeModel.findAll === Seq(DateTimeModel(Id(1), time)))
    }
  }

  test("update") {
    DB.withConnection { implicit conn =>
      DateTimeModel.insert(DateTimeModel(time = new DateTime))
      val time = new DateTime
      DateTimeModel.update(1, DateTimeModel(time = time))
      assert(DateTimeModel.findAll === Seq(DateTimeModel(Id(1), time)))
    }
  }

}
