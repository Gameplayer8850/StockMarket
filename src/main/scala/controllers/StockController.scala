package controllers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import models.DbClasses.StockPrices
import models.stock.{CompanyModel, DividendModel, MarketModel, StockOperationModel}
import services.OAuthService
import services.stock.StockService
import spray.json._


trait StockJsonProtocol extends DefaultJsonProtocol {
  implicit val marketFormat: RootJsonFormat[MarketModel] = jsonFormat5(MarketModel)
  implicit val dividendFormat: RootJsonFormat[DividendModel] = jsonFormat3(DividendModel)
  implicit val companyFormat: RootJsonFormat[CompanyModel] = jsonFormat6(CompanyModel)
  implicit val stockOperationFormat: RootJsonFormat[StockOperationModel] = jsonFormat2(StockOperationModel)
  implicit val stockPricesFormat: RootJsonFormat[StockPrices] = jsonFormat7(StockPrices)
}

object StockController extends StockJsonProtocol with BasicController{
  val route: Route = (path(prefix / "stock" / IntNumber) & get) {
    idCompany => {
      getResult(new StockService(-1).getCompanyData(idCompany))
    }
  } ~ (path(prefix / "stock"/ IntNumber / "buy") & post) { idCompany =>
    {
      authenticateOAuth2(realm = "stock", OAuthService.getCredentials) { idUser =>
        {
          entity(as[StockOperationModel]) { operation =>
            {
              getResult(new StockService(idUser).buyStock(idCompany, operation.amount, operation.price))
            }
          }
        }
      }
    }
  } ~ (path(prefix / "stock"/ IntNumber / "sell") & post) { idCompany =>
  {
    authenticateOAuth2(realm = "stock", OAuthService.getCredentials) { idUser =>
      {
        entity(as[StockOperationModel]) { operation =>
          {
            getResult(new StockService(idUser).sellStock(idCompany, operation.amount, operation.price))
          }
        }
      }
    }
  }
  } ~
    (path(prefix / "stock"/ Segment) & get) { countryShort =>
      getResult(new StockService(-1).getCompaniesForCountry(countryShort))
    }

}
