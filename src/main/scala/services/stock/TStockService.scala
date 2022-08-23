package services.stock

import models.DbClasses.StockPrices
import models.stock.CompanyModel
import services.TService


trait TStockService[F[_]] extends TService{
  def getCompanyData(id: Int): F[AllErrorsOr[CompanyModel]]

  def getCompaniesForCountry(country: String): F[AllErrorsOr[List[StockPrices]]]

  def buyStock(idCompany: Int, amount: Int, price: BigDecimal): F[AllErrorsOr[Boolean]]

  def sellStock(idCompany: Int, amount: Int, price: BigDecimal): F[AllErrorsOr[Boolean]]
}
