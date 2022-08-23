package repositories

import com.typesafe.config.ConfigFactory
import slick.jdbc.SQLServerProfile
import slick.jdbc.SQLServerProfile.api._

import scala.util.{Success, Try}

object DbConnectionManager {
  def configureConnection(): SQLServerProfile.backend.DatabaseDef ={
    val hostName = ConfigFactory.load().getString("configuration.sqlserver.host")
    val port = ConfigFactory.load().getString("configuration.sqlserver.port")
    val databaseName = ConfigFactory.load().getString("configuration.sqlserver.databaseName")
    val user = ConfigFactory.load().getString("configuration.sqlserver.user")
    val password = ConfigFactory.load().getString("configuration.sqlserver.password")

    Database.forURL(s"jdbc:sqlserver://;serverName={$hostName};port={$port};databaseName={$databaseName}", driver="com.microsoft.sqlserver.jdbc.SQLServerDriver", user = user, password = password)
  }

  def testConnection(db: SQLServerProfile.backend.DatabaseDef): Boolean = Try(db.createSession.conn) match {
    case Success(con) => {
      con.close()
      true
    }
    case _ => false
  }
}
