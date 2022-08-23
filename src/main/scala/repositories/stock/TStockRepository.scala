package repositories.stock

import models.DbClasses.{Company, Dividend, Market, Stock, StockPrices}


trait TStockRepository[F[_]] {
  def getStock(id: Int): F[Option[Stock]]

  def getStockAvailableToSale(idUser: Int, idCompany: Int): F[Seq[Stock]]

  def updateStatusStock(idStock: Int, isOnSale: Boolean): F[Int]

  def getCompaniesForMarket(idMarket: Int): F[Seq[StockPrices]]

  def getCompany(id: Int): F[Option[Company]]

  def getMarket(id: Int): F[Option[Market]]

  def getMarketByCountry(countryShort: String): F[Option[Market]]

  def getCompanyDividends(idCompany: Int): F[Seq[Dividend]]
}
