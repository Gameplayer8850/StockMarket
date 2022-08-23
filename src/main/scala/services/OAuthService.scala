package services

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.directives.Credentials
import com.typesafe.config.ConfigFactory
import spray.json._

import java.time.LocalDateTime
import scala.collection.mutable

case class OAToken(accessToken: String = java.util.UUID.randomUUID().toString,
                   tokenType: String = "bearer",
                   expiresIn: Int = ConfigFactory.load().getInt("configuration.oAuth.expirationTimeMillis"))
case class LoggedInUser(basicAuthCredentials: Int,
                        oAToken: OAToken = new OAToken,
                        loggedInAt: LocalDateTime = LocalDateTime.now())

trait OAuthJsonProtocol extends DefaultJsonProtocol {
  implicit val oAuthFormat: RootJsonFormat[OAToken] = jsonFormat3(OAToken)
}
object OAuthService extends OAuthJsonProtocol with SprayJsonSupport {

  private val loggedUsers = mutable.ArrayBuffer.empty[LoggedInUser]

  def generateToken(idUser: Int) : JsValue={
    val user = LoggedInUser(idUser)
    loggedUsers.append(user)
    user.oAToken.toJson
  }

  def getCredentials(credentials: Credentials): Option[Int]={
    credentials match {
      case p @ Credentials.Provided(_) =>{
        loggedUsers.find(user => p.verify(user.oAToken.accessToken))
          .map(x=>x.basicAuthCredentials)
      }
      case _ => None
    }
  }

  def deleteExpiredTokens(): Unit = {
    val users=loggedUsers.filter(user=>user.loggedInAt.plusSeconds(user.oAToken.expiresIn).isBefore(LocalDateTime.now()))
    for(user<-users) loggedUsers-=user
  }

}
