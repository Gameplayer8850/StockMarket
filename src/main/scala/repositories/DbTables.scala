package repositories

import models.DbClasses.{Company, Dividend, Market, Offer, PersonalData, Portfolio, StockPrices, Stock, TransactionCashDetails, TransactionStock, TransactionCash, User, Wallet}
import slick.jdbc.SQLServerProfile.api._

object DbTables {
  class UserTable(tag: Tag) extends Table[User](tag, "USERS"){
    def idUser: Rep[Option[Int]]=column[Option[Int]]("id_user", O.PrimaryKey, O.AutoInc)
    def login: Rep[String]=column[String]("login")
    def password: Rep[String]=column[String]("password")
    def idPersonalData: Rep[Option[Int]]=column[Option[Int]]("id_personal_data")
    def dateCreation: Rep[String]=column[String]("date_creation")
    def dateClose: Rep[Option[String]]=column[Option[String]]("date_close")
    override def * = (idUser, login, password, idPersonalData, dateCreation, dateClose) <>(User.tupled, User.unapply)
  }

  class PersonalDataTable(tag: Tag) extends Table[PersonalData](tag, "PERSONAL_DATA"){
    def idPersonalData: Rep[Option[Int]]=column[Option[Int]]("id_personal_data", O.PrimaryKey, O.AutoInc)
    def firstName: Rep[String]=column[String]("first_name")
    def secondName: Rep[Option[String]]=column[Option[String]]("second_name")
    def surname: Rep[String]=column[String]("surname")
    def dateBirth: Rep[String]=column[String]("date_birth")
    def typeIdentity: Rep[Option[String]]=column[Option[String]]("type_identity")
    def identity: Rep[Option[String]]=column[Option[String]]("identity")
    override def * = (idPersonalData, firstName, secondName, surname, dateBirth, typeIdentity, identity) <>(PersonalData.tupled, PersonalData.unapply)
  }

  class TransactionStockTable(tag: Tag) extends Table[TransactionStock](tag, "TRANSACTIONS_STOCKS"){
    def idTransactionStock: Rep[Option[Int]]=column[Option[Int]]("id_transaction_stock", O.PrimaryKey, O.AutoInc)
    def idStock: Rep[Int]=column[Int]("id_stock")
    def price: Rep[BigDecimal]=column[BigDecimal]("price")
    def idSeller: Rep[Int]=column[Int]("id_seller")
    def idBuyer: Rep[Option[Int]]=column[Option[Int]]("id_buyer")
    def idOfferSell: Rep[Int]=column[Int]("id_offer_sell")
    def idOfferBuy: Rep[Option[Int]]=column[Option[Int]]("id_offer_buy")
    def dateCreation: Rep[String]=column[String]("date_creation")
    def dateSell: Rep[Option[String]]=column[Option[String]]("date_sell")
    override def * = (idTransactionStock, idStock, price, idSeller, idBuyer, idOfferSell, idOfferBuy, dateCreation, dateSell) <>(TransactionStock.tupled, TransactionStock.unapply)
  }

  class TransactionCashTable(tag: Tag) extends Table[TransactionCash](tag, "TRANSACTIONS_CASH"){
    def idTransactionCash: Rep[Int]=column[Int]("id_transaction_cash", O.PrimaryKey, O.AutoInc)
    def idUser: Rep[Int]=column[Int]("id_user")
    def typeTransaction: Rep[String]=column[String]("type")
    def idTransactionStock: Rep[Option[Int]]=column[Option[Int]]("id_transaction_stock")
    def price: Rep[BigDecimal]=column[BigDecimal]("price")
    def date: Rep[String]=column[String]("date")
    override def * = (idTransactionCash, idUser, typeTransaction, idTransactionStock, price, date) <>(TransactionCash.tupled, TransactionCash.unapply)
  }

  class OfferTable(tag: Tag) extends Table[Offer](tag, "OFFERS"){
    def idOffer: Rep[Option[Int]]=column[Option[Int]]("id_offer", O.PrimaryKey, O.AutoInc)
    def typeOffer: Rep[String]=column[String]("type")
    def idCompany: Rep[Int]=column[Int]("id_company")
    def idUser: Rep[Int]=column[Int]("id_user")
    def amountMax: Rep[Int]=column[Int]("amount_max")
    def amountCurrent: Rep[Int]=column[Int]("amount_current")
    def priceMax: Rep[BigDecimal]=column[BigDecimal]("price_max")
    def dateCreation: Rep[String]=column[String]("date_creation")
    def dateUpdate: Rep[Option[String]]=column[Option[String]]("date_update")
    override def * = (idOffer, typeOffer, idCompany, idUser, amountMax, amountCurrent, priceMax, dateCreation, dateUpdate) <>(Offer.tupled, Offer.unapply)
  }

  class MarketTable(tag: Tag) extends Table[Market](tag, "MARKETS"){
    def idMarket: Rep[Int]=column[Int]("id_market", O.PrimaryKey, O.AutoInc)
    def location: Rep[String]=column[String]("location")
    def locationShort: Rep[String]=column[String]("location_short")
    def locationCity: Rep[String]=column[String]("location_city")
    def timeCestOpen: Rep[Option[String]]=column[Option[String]]("time_cest_open")
    def timeCestClose: Rep[Option[String]]=column[Option[String]]("time_cest_close")
    override def * = (idMarket, location, locationShort, locationCity, timeCestOpen, timeCestClose) <>(Market.tupled, Market.unapply)
  }

  class CompanyTable(tag: Tag) extends Table[Company](tag, "COMPANIES"){
    def idCompany: Rep[Int]=column[Int]("id_company", O.PrimaryKey, O.AutoInc)
    def idMarket: Rep[Int]=column[Int]("id_market")
    def fullName: Rep[String]=column[String]("full_name")
    def shortName: Rep[String]=column[String]("short_name")
    def ISIN: Rep[Option[String]]=column[Option[String]]("ISIN")
    override def * = (idCompany, idMarket, fullName, shortName, ISIN) <>(Company.tupled, Company.unapply)
  }

  class StockTable(tag: Tag) extends Table[Stock](tag, "STOCKS"){
    def idStock: Rep[Option[Int]]=column[Option[Int]]("id_stock", O.PrimaryKey, O.AutoInc)
    def idCompany: Rep[Int]=column[Int]("id_company")
    def idCurrentOwner: Rep[Int]=column[Int]("id_current_owner")
    def isOnSale: Rep[Boolean]=column[Boolean]("is_on_sale")
    def dateCreate: Rep[String]=column[String]("date_create")
    override def * = (idStock, idCompany, idCurrentOwner, isOnSale, dateCreate) <>(Stock.tupled, Stock.unapply)
  }

  class DividendTable(tag: Tag) extends Table[Dividend](tag, "DIVIDENDS"){
    def idDividend: Rep[Int]=column[Int]("id_dividend", O.PrimaryKey, O.AutoInc)
    def idCompany: Rep[Int]=column[Int]("id_company")
    def dateEx: Rep[String]=column[String]("date_ex")
    def datePay: Rep[String]=column[String]("date_pay")
    def amountInDolars: Rep[BigDecimal]=column[BigDecimal]("amount_in_dolars")
    override def * = (idDividend, idCompany, dateEx, datePay, amountInDolars) <>(Dividend.tupled, Dividend.unapply)
  }

  //views
  class StockPricesTable(tag: Tag) extends Table[StockPrices](tag, "Stock_Prices"){
    def idCompany: Rep[Int]=column[Int]("id_company")
    def idMarket: Rep[Int]=column[Int]("id_market")
    def fullName: Rep[String]=column[String]("full_name")
    def shortName: Rep[String]=column[String]("short_name")
    def ISIN: Rep[Option[String]]=column[Option[String]]("ISIN")
    def priceSell: Rep[Option[BigDecimal]]=column[Option[BigDecimal]]("price_sell")
    def priceBuy: Rep[Option[BigDecimal]]=column[Option[BigDecimal]]("price_buy")
    override def * = (idCompany, idMarket, fullName, shortName, ISIN, priceSell, priceBuy) <> (StockPrices.tupled, StockPrices.unapply)
  }

  class PortfolioTable(tag: Tag) extends Table[Portfolio](tag, "Portfolio"){
    def idCurrentOwner: Rep[Int]=column[Int]("id_current_owner")
    def idCompany: Rep[Int]=column[Int]("id_company")
    def shortName: Rep[String]=column[String]("short_name")
    def fullName: Rep[String]=column[String]("full_name")
    def amount: Rep[Int]=column[Int]("amount")
    override def * = (idCurrentOwner, idCompany, shortName, fullName, amount) <> (Portfolio.tupled, Portfolio.unapply)
  }

  class WalletTable(tag: Tag) extends Table[Wallet](tag, "Wallet"){
    def idUser: Rep[Int]=column[Int]("id_user")
    def balance: Rep[BigDecimal]=column[BigDecimal]("balance")
    def remainingValue: Rep[BigDecimal]=column[BigDecimal]("remaining_value")
    def availableBalance: Rep[BigDecimal]=column[BigDecimal]("available_balance")
    override def * =(idUser, balance, remainingValue, availableBalance) <> (Wallet.tupled, Wallet.unapply)
  }

  class TransactionCashDetailsTable(tag: Tag) extends Table[TransactionCashDetails](tag, "Transaction_Cash_Details"){
    def idTransactionCash: Rep[Int]=column[Int]("id_transaction_cash")
    def idUser: Rep[Int]=column[Int]("id_user")
    def typeTransaction: Rep[String]=column[String]("type")
    def idTransactionStock: Rep[Option[Int]]=column[Option[Int]]("id_transaction_stock")
    def price: Rep[BigDecimal]=column[BigDecimal]("price")
    def date: Rep[String]=column[String]("date")
    def idCompany: Rep[Option[Int]]=column[Option[Int]]("id_company")
    override def * =(idTransactionCash, idUser, typeTransaction, idTransactionStock, price, date, idCompany) <> (TransactionCashDetails.tupled, TransactionCashDetails.unapply)
  }
}
