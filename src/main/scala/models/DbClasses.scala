package models

object DbClasses {
  case class User(idUser: Option[Int], login: String, password: String, idPersonalData: Option[Int], dateCreation:String, dateClose:Option[String])
  case class PersonalData(idPersonalData: Option[Int], firstName: String, secondName: Option[String], surname: String, dateBirth: String, typeIdentity: Option[String], identity: Option[String])

  case class TransactionStock(idTransactionStock: Option[Int], idStock: Int, price: BigDecimal, idSeller: Int, idBuyer: Option[Int], idOfferSell: Int, idOfferBuy: Option[Int], dateCreation: String, dateSell: Option[String])
  case class TransactionCash(idTransactionCash: Int, idUser: Int, typeTransaction: String, idTransactionStock: Option[Int], price: BigDecimal, date: String)
  case class Offer(idOffer: Option[Int], typeOffer: String, idCompany: Int, idUser: Int, amountMax: Int, amountCurrent: Int, priceMax: BigDecimal, dateCreation: String, dateUpdate: Option[String])

  case class Market(idMarket: Int, location: String, locationShort: String, locationCity: String, timeCestOpen: Option[String], timeCestClose: Option[String])
  case class Company(idCompany: Int, idMarket: Int, fullName: String, shortName: String, ISIN: Option[String])
  case class Stock(idStock: Option[Int], idCompany: Int, idCurrentOwner: Int, isOnSale: Boolean, dateCreate: String)
  case class Dividend(idDividend: Int, idCompany: Int, dateEx: String, datePay: String, amountInDolars: BigDecimal)

  //views
  case class StockPrices(idCompany: Int, idMarket: Int, fullName: String, shortName: String, ISIN: Option[String], priceSell: Option[BigDecimal], priceBuy: Option[BigDecimal])
  case class Portfolio(idCurrentOwner: Int, idCompany: Int, shortName: String, fullName: String, amount: Int)
  case class Wallet(idUser: Int, balance: BigDecimal, remainingValue: BigDecimal, availableBalance: BigDecimal)
  case class TransactionCashDetails(idTransactionCash: Int, idUser: Int, typeTransaction: String, idTransactionStock: Option[Int], price: BigDecimal, date: String, idCompany: Option[Int])
}
