package com.derekschaefer.anorm.postgresql

import anorm._
import anorm.SqlParser._

import org.joda.time.Period

import java.sql.Connection

import org.scalatest.{FunSuite, BeforeAndAfter}

import JodaTime._

case class PeriodModel(id: Pk[Long] = NotAssigned, period: Period)

object PeriodModel {
  val table = "test_period"

  val simple = {
    get[Pk[Long]](s"$table.id") ~
    get[Period](s"$table.period") map {
      case id~period => PeriodModel(id,period)
    }
  }

  def findAll(implicit conn: Connection): Seq[PeriodModel] = {
    SQL(s"select * from $table").as(simple *)
  }

  def insert(model: PeriodModel)(implicit conn: Connection): Option[Long] = {
    SQL(s"insert into $table (period) values ({period})").on(
      'period -> model.period
    ).executeInsert()
  }

  def update(id: Long, model: PeriodModel)(implicit conn: Connection): Int = {
    SQL(s"update $table set period = {period} where id = {id}").on(
      'id -> id,
      'period -> model.period
    ).executeUpdate()
  }

}

class PeriodTest extends FunSuite with BeforeAndAfter {

  before {
    DB.withConnection { implicit conn =>
      SQL(
        """
        |create table if not exists test_period (
        |  id serial primary key not null,
        |  period interval not null
        |)""".stripMargin
      ).execute
    }
  }


  after {
    DB.withConnection { implicit conn =>
      SQL("drop table test_period").execute
    }
  }

  test("insert") {
    DB.withConnection { implicit conn =>
      val period = new Period(1,1,1,1,1,1,1,1)
      PeriodModel.insert(PeriodModel(period = period))
      assert(PeriodModel.findAll === Seq(PeriodModel(Id(1),period.normalizedStandard)))
    }
  }

  test("insert with random weeks") {
    DB.withConnection { implicit conn =>
      val period = new Period(1,1,4,1,1,1,1,1)
      PeriodModel.insert(PeriodModel(period = period))
      assert(PeriodModel.findAll === Seq(PeriodModel(Id(1),period.normalizedStandard)))
    }
  }

  test("days normalized") {
    DB.withConnection { implicit conn =>
      val period = new Period(1,1,1,47,1,1,1,1)
      PeriodModel.insert(PeriodModel(period = period))
      assert(PeriodModel.findAll === Seq(PeriodModel(Id(1),period.normalizedStandard)))
    }
  }

  test("update") {
    DB.withConnection { implicit conn =>
      val period = new Period(1,1,1,1,1,1,1,0)
      PeriodModel.insert(PeriodModel(period = period))
      val period2 = new Period(1,1,1,1,1,1,2,0)
      PeriodModel.update(1,PeriodModel(period = period2))
      assert(PeriodModel.findAll === Seq(PeriodModel(Id(1),period2)))
    }
  }

}
