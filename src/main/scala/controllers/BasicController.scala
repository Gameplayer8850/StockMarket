package controllers

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import cats.data.{NonEmptyChain, ValidatedNec}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.data.Validated.{Invalid, Valid}
import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import models.{ResponseValidationErrorModel, ValidationErrorModel}
import spray.json.{RootJsonFormat, _}

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait BasicJsonProtocol extends DefaultJsonProtocol{
  implicit val responseValidationErrorFormat: RootJsonFormat[ResponseValidationErrorModel] = jsonFormat1(ResponseValidationErrorModel)
}

trait BasicController extends BasicJsonProtocol with SprayJsonSupport with LazyLogging{

  val errorMessage: String = ConfigFactory.load().getString("configuration.apiServer.errorMessage")
  val prefix: String = ConfigFactory.load().getString("configuration.apiServer.prefix")

  implicit object MyBooleanJsonFormat extends RootJsonFormat[Boolean] {
    def write(value: Boolean): JsBoolean = JsBoolean(value)

    def read(value: JsValue): Boolean = {
      value match {
        case JsBoolean(result) => result
        case _ => throw DeserializationException("Invalid Boolean")
      }
    }
  }

  def getErrorMessages(errorList: NonEmptyChain[ValidationErrorModel]): List[ResponseValidationErrorModel]={
    logger.info(s"Service returned validation errors: $errorList")
    errorList.map(x=>ResponseValidationErrorModel(x.errorMessage)).toNonEmptyList.toList
  }

  def getResult[A](result: Future[ValidatedNec[ValidationErrorModel, A]])(implicit jsonProtocol: RootJsonFormat[A]): Route={
    onComplete(result){
      case Success(right) => {
        right match {
          case Valid(x) => complete(x)
          case Invalid(y) => complete(BadRequest, getErrorMessages(y))
        }
      }
      case Failure(left) => {
        logger.error("Service threw an error: "+ left.getMessage)
        complete(InternalServerError, errorMessage)
      }
    }
  }

  def getResult[A](result: Future[ValidatedNec[ValidationErrorModel, Option[A]]], message: String)(implicit jsonProtocol: RootJsonFormat[A]): Route={
    onComplete(result){
      case Success(right) => {
        right match {
          case Valid(Some(x)) => complete(x)
          case Valid(None) => complete(InternalServerError, message)
          case Invalid(y) => complete(BadRequest, getErrorMessages(y))
        }
      }
      case Failure(left) => {
        logger.error("Service threw an error: "+ left.getMessage)
        complete(InternalServerError, errorMessage)
      }
    }
  }

  def getResultAndCheckBoolean(result: Future[ValidatedNec[ValidationErrorModel, Boolean]], message: String)(implicit jsonProtocol: RootJsonFormat[Boolean]): Route={
    onComplete(result){
      case Success(right) => {
        right match {
          case Valid(x) =>
            if (x) complete(x)
            else complete(BadRequest, message)
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
