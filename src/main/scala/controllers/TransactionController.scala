package controllers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import models.DbClasses.{Offer, Portfolio, TransactionCashDetails, Wallet}
import models.transaction.OfferUpdateModel
import services.OAuthService
import services.transaction.TransactionService
import spray.json._

trait TransactionJsonProtocol extends DefaultJsonProtocol {
  implicit val portfolioFormat: RootJsonFormat[Portfolio] = jsonFormat5(Portfolio)
  implicit val oBuyFormat: RootJsonFormat[Offer] = jsonFormat9(Offer)
  implicit val walletFormat: RootJsonFormat[Wallet] = jsonFormat4(Wallet)
  implicit val transactionCashDetailsFormat: RootJsonFormat[TransactionCashDetails] = jsonFormat7(TransactionCashDetails)
  implicit val offerUpdateFormat: RootJsonFormat[OfferUpdateModel] = jsonFormat2(OfferUpdateModel)
}

object TransactionController extends TransactionJsonProtocol with BasicController{
  val route: Route = (path(prefix / "portfolio" ) & get){
    authenticateOAuth2(realm = "portfolio", OAuthService.getCredentials){
      idUser =>{
        getResult(new TransactionService(idUser).getUserPortfolio)
      }
    }
  } ~ (path(prefix / "wallet" ) & get){
    authenticateOAuth2(realm = "wallet", OAuthService.getCredentials){
      idUser =>{
        getResult(new TransactionService(idUser).getUserWallet, "Wallet not found.")
      }
    }
  } ~ (path(prefix / "transaction" ) & get){
    authenticateOAuth2(realm = "transaction", OAuthService.getCredentials){
      idUser =>{
        getResult(new TransactionService(idUser).getUserTransactions)
      }
    }
  } ~ (path(prefix / "transaction" / "sell" ) & get){
    authenticateOAuth2(realm = "sell", OAuthService.getCredentials){
      idUser =>{
        getResult(new TransactionService(idUser).getUserOffersSell)
      }
    }
  }  ~ (path(prefix / "transaction" / "sell" / IntNumber) & put){
    id =>{
      authenticateOAuth2(realm = "sell", OAuthService.getCredentials){
        idUser =>{
          entity(as[OfferUpdateModel]){ operation=>
            {
              getResult(new TransactionService(idUser).updateOffer(id, "S", operation))
            }
          }
        }
      }
    }
  } ~ (path(prefix / "transaction" / "sell" / IntNumber) & delete){
    id =>{
      authenticateOAuth2(realm = "sell", OAuthService.getCredentials){
        idUser =>{
          getResult(new TransactionService(idUser).deleteOffer(id, "S"))
        }
      }
    }
  } ~ (path(prefix / "transaction" / "buy" ) & get){
    authenticateOAuth2(realm = "buy", OAuthService.getCredentials){
      idUser =>{
        getResult(new TransactionService(idUser).getUserOffersBuy)
      }
    }
  } ~ (path(prefix / "transaction" / "buy" / IntNumber) & put){
    id =>{
      authenticateOAuth2(realm = "buy", OAuthService.getCredentials){
        idUser =>{
          entity(as[OfferUpdateModel]){ operation=>
            {
              getResult(new TransactionService(idUser).updateOffer(id, "B", operation))
            }
          }
        }
      }
    }
  } ~ (path(prefix / "transaction" / "buy" / IntNumber) & delete){
    id =>{
      authenticateOAuth2(realm = "buy", OAuthService.getCredentials){
        idUser =>{
          getResult(new TransactionService(idUser).deleteOffer(id, "B"))
        }
      }
    }
  }
}
