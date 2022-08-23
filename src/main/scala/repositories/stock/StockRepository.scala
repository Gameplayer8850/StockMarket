package repositories.stock

import models.DbClasses.{Company, Dividend, Market, Stock, StockPrices}
import repositories.DbTables.{CompanyTable, DividendTable, MarketTable, StockPricesTable, StockTable}
import slick.jdbc.SQLServerProfile
import slick.lifted.TableQuery
import slick.jdbc.SQLServerProfile.api._

import scala.concurrent.Future

class StockRepository(db: SQLServerProfile.backend.DatabaseDef) extends TStockRepository[Future]{
  private lazy val companyTable= TableQuery[CompanyTable]
  private lazy val marketTable= TableQuery[MarketTable]
  private lazy val dividendTable= TableQuery[DividendTable]
  private lazy val stockTable = TableQuery[StockTable]
  private lazy val stockPricesTable = TableQuery[StockPricesTable]

  def getStock(id: Int): Future[Option[Stock]] =
    db.run(
      stockTable
        .filter(_.idStock === id)
        .take(1)
        .result
        .headOption)

  def getStockAvailableToSale(idUser: Int, idCompany: Int): Future[Seq[Stock]] =
    db.run(
      stockTable
        .filter(x=>x.idCurrentOwner===idUser && x.idCompany===idCompany && !x.isOnSale)
        .result)

  def updateStatusStock(idStock: Int, isOnSale: Boolean): Future[Int] = db.run(
       stockTable
         .filter(_.idStock === idStock)
         .map(x=>x.isOnSale)
         .update(isOnSale))

  def getCompaniesForMarket(idMarket: Int): Future[Seq[StockPrices]] =
    db.run(
      stockPricesTable
        .filter(_.idMarket === idMarket)
        .result)

  def getCompany(id: Int): Future[Option[Company]] =
    db.run(
      companyTable
        .filter(_.idCompany === id)
        .take(1)
        .result
        .headOption)

  def getMarket(id: Int): Future[Option[Market]] =
    db.run(
      marketTable
        .filter(_.idMarket === id)
        .take(1)
        .result
        .headOption)

  def getMarketByCountry(countryShort: String): Future[Option[Market]] =
    db.run(
      marketTable
        .filter(_.locationShort === countryShort)
        .take(1)
        .result
        .headOption)

  def getCompanyDividends(idCompany: Int): Future[Seq[Dividend]] =
    db.run(
      dividendTable
        .filter(_.idCompany === idCompany)
        .result)

  def runMatchTransactionsProcedure(): Future[Vector[String]] =
    db.run(sql"EXEC MatchTransactions".as[String])
}
