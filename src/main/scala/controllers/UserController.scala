package controllers

import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNec
import models.ValidationErrorModel
import models.user.{SingInModel, UserModel}
import services.OAuthService
import services.user.UserService
import spray.json._

import scala.util.{Failure, Success}

trait UserJsonProtocol extends DefaultJsonProtocol {
  implicit val singInFormat: RootJsonFormat[SingInModel] = jsonFormat2(SingInModel)
  implicit val userModelFormat: RootJsonFormat[UserModel] = jsonFormat8(UserModel)
}

object UserController extends UserJsonProtocol with BasicController{
  val route: Route = (path(prefix / "user"/ "singin") & post) {
    entity(as[SingInModel]){ singIn =>
      {
        onComplete(new UserService(-1).signIn(singIn.login, singIn.password)){
          case Success(right) => {
            right match {
              case Valid(Some(x)) => complete(OAuthService.generateToken(x))
              case Valid(None) => complete(BadRequest, "Wrong login or password.")
              case Invalid(y) => complete(BadRequest, getErrorMessages(y))
            }
          }
          case Failure(left) => {
            logger.error("Service threw an error: "+ left.getMessage)
            complete(InternalServerError, errorMessage)
          }
        }
      }
    }
  } ~
    (path(prefix / "user"/ "test") & get){
      authenticateOAuth2(realm = "test", OAuthService.getCredentials) { idUser =>
        complete(s"Hey! user = $idUser")
      }
    } ~
    (path(prefix / "user") & get){
      authenticateOAuth2(realm = "user", OAuthService.getCredentials) { idUser =>
        {
          getResult(new UserService(idUser).getUserData, "Detailed user information could not be found.")
        }
      }
    } ~
    (path(prefix / "user") & put){
      authenticateOAuth2(realm = "user", OAuthService.getCredentials) { idUser =>
        {
          entity(as[UserModel]){ newUser =>
            {
              getResultAndCheckBoolean(new UserService(idUser).update(newUser), "The user update failed.")
            }
          }
        }
      }
    } ~
    (path(prefix / "user") & post) {
      entity(as[UserModel]) { newUser =>
        {
          getResultAndCheckBoolean(new UserService(-1).create(newUser), "The user create failed.")
        }
      }
    }
}
