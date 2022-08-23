package services

import com.typesafe.scalalogging.LazyLogging
import repositories.DbConnectionManager

import scala.concurrent.Future

abstract class BasicService(idUser: Int, serviceName: String, logCreator: Boolean=true) extends LazyLogging{
  if(logCreator) logger.debug(s"Created object of {$serviceName} for user with id: {$idUser}")

  val dbConnection = DbConnectionManager.configureConnection()
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  def log(message: String): Future[Unit]= Future(logger.debug(message))
}
