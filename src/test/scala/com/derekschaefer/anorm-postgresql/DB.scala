package com.derekschaefer.anorm.postgresql

import java.sql.{Connection, DriverManager}

object DB {

  val db = "test"
  val url = "jdbc:postgresql://localhost/%s".format(db)
  val user = "test"
  val password = "test"

  val driver = Class.forName("org.postgresql.Driver")

  def withConnection[T](block: Connection => T): T = {
    val connection = DriverManager.getConnection(url, user, password)
    try {
      block(connection)
    } finally {
      connection.close()
    }
  }

}
