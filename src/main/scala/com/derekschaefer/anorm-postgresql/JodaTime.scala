package com.derekschaefer.anorm.postgresql

import anorm._

import org.joda.time._
import org.joda.time.format._

import org.postgresql.util.PGInterval

object JodaTime {

  val dateParser = ISODateTimeFormat.dateParser()
  val timeParser = ISODateTimeFormat.timeParser()
  val dateTimeParser = ISODateTimeFormat.dateTimeParser()
  private val mathContext = new java.math.MathContext(4)

  implicit def rowToLocalDate: Column[LocalDate] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case d: java.sql.Date => Right(new LocalDate(d.getTime))
      case str: java.lang.String => Right(new LocalDate(dateParser.parseLocalDate(str)))
      case _ => Left(TypeDoesNotMatch("Derp"))
    }
  }

  implicit def localDateToStatement = new ToStatement[LocalDate] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: LocalDate) {
      s.setDate(index, new java.sql.Date(aValue.toDateTimeAtStartOfDay.getMillis))
    }
  }

  implicit def rowToLocalTime: Column[LocalTime] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case t: java.sql.Time => Right(new LocalTime(t.getTime))
      case str: java.lang.String => Right(new LocalTime(timeParser.parseLocalTime(str)))
      case _ => Left(TypeDoesNotMatch("Derp"))
    }
  }

  implicit def localTimeToStatement = new ToStatement[LocalTime] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: LocalTime) {
      s.setTime(index, new java.sql.Time(aValue.toDateTimeToday.getMillis))
    }
  }

  implicit def rowToDateTime: Column[DateTime] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case ts: java.sql.Timestamp => Right(new DateTime(ts.getTime))
      case str: java.lang.String => Right(new DateTime(dateTimeParser.parseDateTime(str)))
      case _ => Left(TypeDoesNotMatch("Derp"))
    }
  }

  implicit def dateTimeToStatement = new ToStatement[DateTime] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: DateTime) {
      s.setTimestamp(index, new java.sql.Timestamp(aValue.getMillis))
    }
  }

  implicit def rowToLocalDateTime: Column[LocalDateTime] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case ts: java.sql.Timestamp => Right(new LocalDateTime(ts.getTime))
      case str: java.lang.String => Right(new LocalDateTime(dateTimeParser.parseLocalDateTime(str)))
      case _ => Left(TypeDoesNotMatch("Derp"))
    }
  }

  implicit def localDateTimeToStatement = new ToStatement[LocalDateTime] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: LocalDateTime) {
      s.setTimestamp(index, new java.sql.Timestamp(aValue.toDateTime.getMillis))
    }
  }

  implicit val rowToPeriod: Column[Period] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified,nullable, clazz) = meta
    value match {
      case ts: org.postgresql.util.PGInterval => {
        val years = ts.getYears
        val months = ts.getMonths
        val days = ts.getDays
        val hours = ts.getHours
        val mins = ts.getMinutes
        val seconds = Math.floor(ts.getSeconds).asInstanceOf[Int]
        val secondsAsBigDecimal = new java.math.BigDecimal(ts.getSeconds,mathContext)
        val millis = secondsAsBigDecimal.subtract(new java.math.BigDecimal(seconds)).multiply(new java.math.BigDecimal(1000)).intValue

        Right(new Period(years,months, 0, days, hours, mins, seconds,millis).normalizedStandard)
      }
      case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" + value.asInstanceOf[AnyRef].getClass) )
    }
  }

  implicit val periodToStatement = new ToStatement[Period] {
    def set(s: java.sql.PreparedStatement, index: Int, aValue: Period): Unit = {
      val years = aValue.getYears
      val months = aValue.getMonths
      val days = aValue.getWeeks*7 + aValue.getDays
      val hours = aValue.getHours
      val mins = aValue.getMinutes
      val seconds = aValue.getSeconds
      val millis = aValue.getMillis
      val subseconds = new java.math.BigDecimal(millis,mathContext).divide(new java.math.BigDecimal(1000))
      val combinedSecond = new java.math.BigDecimal(aValue.getSeconds,mathContext).add(subseconds).doubleValue

      val interval = new PGInterval(years,months,days,hours,mins,combinedSecond)
      s.setObject(index,interval)
    }
  }

}
