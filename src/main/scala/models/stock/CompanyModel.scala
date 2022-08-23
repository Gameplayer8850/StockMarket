package models.stock

case class CompanyModel(idCompany: Int, fullName: String, shortName: String, ISIN: Option[String], dividendsHistory: Option[List[DividendModel]], market: Option[MarketModel])
