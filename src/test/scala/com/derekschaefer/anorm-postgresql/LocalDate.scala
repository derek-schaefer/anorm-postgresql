package com.derekschaefer.anorm.postgresql

import anorm._
import anorm.SqlParser._

import org.joda.time._

import java.sql.Connection

import org.scalatest.{FunSuite, BeforeAndAfter}

import JodaTime._

case class LocalDateModel(id: Pk[Long] = NotAssigned, date: LocalDate)

object LocalDateModel {

  val table = "test_localdate"

  val simple = {
    get[Pk[Long]](s"$table.id") ~
    get[LocalDate](s"$table.date") map {
      case id~date => LocalDateModel(id, date)
    }
  }

  def findAll(implicit conn: Connection): Seq[LocalDateModel] = {
    SQL(s"select * from $table").as(simple *)
  }

  def insert(model: LocalDateModel)(implicit conn: Connection): Option[Long] = {
    SQL(s"insert into $table (date) values ({date})").on(
      'date -> model.date
    ).executeInsert()
  }

  def update(id: Long, model: LocalDateModel)(implicit conn: Connection): Int = {
    SQL(s"update $table set date = {date} where id = {id}").on(
      'id -> id,
      'date -> model.date
    ).executeUpdate()
  }

}

class LocalDateTest extends FunSuite with BeforeAndAfter {

  before {
    DB.withConnection { implicit conn =>
      SQL(
        """
        |create table if not exists test_localdate (
        |  id serial primary key not null,
        |  date date
        |)""".stripMargin
      ).execute
    }
  }

  after {
    DB.withConnection { implicit conn =>
      SQL("drop table test_localdate").execute
    }
  }

  test("insert") {
    DB.withConnection { implicit conn =>
      val date = new LocalDate
      LocalDateModel.insert(LocalDateModel(date = date))
      assert(LocalDateModel.findAll === Seq(LocalDateModel(Id(1), date)))
    }
  }

  test("update") {
    DB.withConnection { implicit conn =>
      LocalDateModel.insert(LocalDateModel(date = new LocalDate))
      val date = (new LocalDate).plusDays(1)
      LocalDateModel.update(1, LocalDateModel(date = date))
      assert(LocalDateModel.findAll === Seq(LocalDateModel(Id(1), date)))
    }
  }

}
