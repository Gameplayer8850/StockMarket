package services.stock

import cats.syntax.all._
import com.typesafe.scalalogging.LazyLogging
import models.DbClasses.{Company, Market, Stock, StockPrices}
import models.ValidationErrorModel
import models.stock.{CompanyModel, DividendModel, MarketModel}
import repositories.stock.StockRepository
import services.BasicService
import services.transaction.TransactionService

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

class StockService(idUser: Int, logCreator: Boolean=true) extends BasicService(idUser, "StockService", logCreator) with TStockService[Future]{
  val stockRepository = new StockRepository(dbConnection)

  def getCompanyData(id: Int): Future[AllErrorsOr[CompanyModel]] ={
    Future {
      logger.debug(s"Running service called 'getCompanyData' with parameters {id: {$id}}")
      StockValidationError.companyValidate(Await.result(stockRepository.getCompany(id), 30.seconds)) match {
        case Left(companyError) => companyError.invalidNec
        case Right(company) => {
          val market = Await.result(stockRepository.getMarket(company.idMarket), 30.seconds)
            .map(x=>MarketModel(x.idMarket, x.location, x.locationCity, x.timeCestOpen, x.timeCestClose))
          val dividendHistory=Option[List[DividendModel]](Await.result(stockRepository.getCompanyDividends(company.idCompany), 30.seconds)
            .map(x=>DividendModel(x.dateEx, x.datePay, x.amountInDolars)).toList)
          CompanyModel(id, company.fullName, company.shortName, company.ISIN, dividendHistory, market).validNec
        }
      }
    }
  }

  def getCompaniesForCountry(countryShort: String): Future[AllErrorsOr[List[StockPrices]]] = {
    Future {
      logger.debug(s"Running service called 'getCompaniesForCountry' with parameters {countryShort: {$countryShort}}")
      StockValidationError.countryValidate(Await.result(stockRepository.getMarketByCountry(countryShort.toUpperCase()), 30.seconds)) match {
        case Left(marketError) => marketError.invalidNec
        case Right(market) => {
          Await.result(stockRepository.getCompaniesForMarket(market.idMarket), 30.seconds).toList.validNec
        }
      }
    }
  }

  def buyStock(idCompany: Int, amount: Int, price: BigDecimal): Future[AllErrorsOr[Boolean]]={
    Future {
      logger.debug(s"Running service called 'buyStock' with parameters {idCompany: {$idCompany}, amount: {$amount}, price: {$price}}")
      val transactionService = new TransactionService(idUser)
      StockValidationError.availableBalanceValidate(transactionService.getUserWalletAvailableBalance, amount, price) match {
        case Left(availableBalanceError) => availableBalanceError.invalidNec
        case _ => {
          StockValidationError.offerValidate(transactionService.createOffer("B", idCompany, amount, price)) match {
            case Left(offerError) => offerError.invalidNec
            case _ => true.validNec
          }
        }
      }
    }
  }

  def sellStock(idCompany: Int, amount: Int, price: BigDecimal): Future[AllErrorsOr[Boolean]]={
    Future {
      logger.debug(s"Running service called 'sellStock' with parameters {idCompany: {$idCompany}, amount: {$amount}, price: {$price}}")
      val transactionService = new TransactionService(idUser)
      StockValidationError.availableStocksValidate(getStockAvailableToSale(idCompany), amount) match {
        case Left(stocksError) => stocksError.invalidNec
        case Right(stocks) => {
          StockValidationError.offerValidate(transactionService.createOffer("S", idCompany, amount, price)) match {
            case Left(offerError) => offerError.invalidNec
            case Right(idOffer) => {
              for (stock <- stocks.take(amount)) {
                addStocksToTransaction(idOffer, price, stock)
              }
              true.validNec
            }
          }
        }
      }
    }
  }

  def getStockAvailableToSale(idCompany: Int): Seq[Stock] ={
    Await.result(stockRepository.getStockAvailableToSale(idUser, idCompany), 30.seconds)
  }

  def addStocksToTransaction(idOffer:Int, price:BigDecimal, stock: Stock): Unit ={
    val transactionService=new TransactionService(idUser)
    updateStatusStock(stock.idStock.get, isOnSale = true)
    transactionService.createTransaction(idOffer, stock.idStock.get, price)
  }

  def updateStatusStock(idStock: Int, isOnSale: Boolean): Unit ={
    Await.result(stockRepository.updateStatusStock(idStock, isOnSale), 30.seconds)
  }

  def runMatchTransactionsProcedure(): Future[Vector[String]] = stockRepository.runMatchTransactionsProcedure()

  sealed trait StockValidationError extends ValidationErrorModel

  object StockValidationError {
    def companyValidate(companyOption: Option[Company]): Either[StockValidationError, Company] = {
      companyOption.map(Right(_))
        .getOrElse(Left(CompanyInvalid))
    }

    def countryValidate(marketOption: Option[Market]): Either[StockValidationError, Market] = {
      marketOption.map(Right(_))
        .getOrElse(Left(CountryInvalid))
    }

    def availableBalanceValidate(availableBalanceOption: Option[BigDecimal], amount: Int, price: BigDecimal): Either[StockValidationError, Unit] = {
      availableBalanceOption.flatMap(x=>if(x-(amount*price)<0) None else Option(Right()))
        .getOrElse(Left(AvailableBalanceInvalid))
    }

    def offerValidate(idOffer: Option[Int]): Either[StockValidationError, Int] = {
      idOffer.flatMap(x=>if(x<1) None else Option(Right(x)))
        .getOrElse(Left(OfferInvalid))
    }

    def availableStocksValidate(stocks: Seq[Stock], amount: Int): Either[StockValidationError, Seq[Stock]] = {
      if(stocks.isEmpty || stocks.length<amount) Left(AvailableStocksInvalid)
      else Right(stocks)
    }

    final case object CompanyInvalid extends StockValidationError {
      override def errorMessage: String = "Invalid stock with given id."
    }

    final case object CountryInvalid extends StockValidationError {
      override def errorMessage: String = "Invalid country."
    }

    final case object AvailableBalanceInvalid extends StockValidationError {
      override def errorMessage: String = "You cannot submit this offer with available balance."
    }

    final case object OfferInvalid extends StockValidationError {
      override def errorMessage: String = "The offer create failed."
    }

    final case object AvailableStocksInvalid extends StockValidationError {
      override def errorMessage: String = "You cannot submit this offer with your stocks portfolio."
    }
  }
}
