import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import controllers.{StockController, TransactionController, UserController}
import services.OAuthService
import services.stock.StockService

import scala.concurrent.duration.DurationInt

object StockMarket extends LazyLogging{
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "StockMarket")

  object MainRouter {
    val routes:Route = UserController.route ~ StockController.route ~ TransactionController.route
  }

  def main(args: Array[String]): Unit ={
    logger.debug("Running the server")

    //api server
    Http().newServerAt(ConfigFactory.load().getString("configuration.apiServer.interface"), ConfigFactory.load().getInt("configuration.apiServer.port")).bind(MainRouter.routes)

    //check and delete expired tokens
    system.scheduler.scheduleWithFixedDelay(ConfigFactory.load().getInt("configuration.oAuth.deleteInitialDelayMillis").millis, ConfigFactory.load().getInt("configuration.oAuth.deleteDelayMillis").millis)(() => OAuthService.deleteExpiredTokens())(system.executionContext)

    //run "transaction matcher", that matches and closes buy/sell offers
    system.scheduler.scheduleWithFixedDelay(ConfigFactory.load().getInt("configuration.matchTransaction.execInitialDelayMillis").millis, ConfigFactory.load().getInt("configuration.matchTransaction.execDelayMillis").millis)(() => new StockService(idUser = -1, logCreator = false).runMatchTransactionsProcedure())(system.executionContext)
  }
}
