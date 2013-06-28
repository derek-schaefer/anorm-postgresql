package com.derekschaefer.anorm.postgresql

import anorm._

import org.joda.time._
import org.joda.time.format._

object JodaTime {

  val dateParser = ISODateTimeFormat.dateParser()
  val timeParser = ISODateTimeFormat.timeParser()
  val dateTimeParser = ISODateTimeFormat.dateTimeParser()

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

}
