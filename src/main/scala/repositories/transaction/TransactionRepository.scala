package repositories.transaction

import models.DbClasses.{Offer, Portfolio, TransactionCashDetails, TransactionStock, Wallet}
import repositories.DbTables.{OfferTable, PortfolioTable, TransactionCashDetailsTable, TransactionStockTable, WalletTable}
import slick.jdbc.SQLServerProfile
import slick.jdbc.SQLServerProfile.api._

import scala.concurrent.Future

class TransactionRepository(db: SQLServerProfile.backend.DatabaseDef) extends TTransactionRepository[Future]{
  private lazy val portfolioTable = TableQuery[PortfolioTable]
  private lazy val offerTable = TableQuery[OfferTable]
  private lazy val walletTable = TableQuery[WalletTable]
  private lazy val transactionCashDetailsTable = TableQuery[TransactionCashDetailsTable]
  private lazy val transactionStockTable = TableQuery[TransactionStockTable]

  def getUserPortfolio(idUser: Int): Future[Seq[Portfolio]] =
    db.run(
      portfolioTable
        .filter(_.idCurrentOwner === idUser)
        .result)

  def getUserOffers(idUser: Int, typeOffer: String): Future[Seq[Offer]] =
    db.run(
      offerTable
        .filter(x=>x.idUser === idUser && x.typeOffer===typeOffer && x.amountCurrent =!= x.amountMax)
        .result)

  def getUserWallet(idUser: Int): Future[Option[Wallet]] =
    db.run(
      walletTable
        .filter(_.idUser === idUser)
        .take(1)
        .result
        .headOption)

  def getUserTransactions(idUser: Int): Future[Seq[TransactionCashDetails]] =
    db.run(
      transactionCashDetailsTable
        .filter(_.idUser === idUser)
        .result)

  def getOffer(id: Int): Future[Option[Offer]] =
    db.run(
      offerTable
      .filter(_.idOffer === id)
      .take(1)
      .result
      .headOption)

  def insertOffer(offer: Offer): Future[Option[Int]] = {
    db.run((offerTable returning offerTable.map(_.idOffer)) += offer)
  }

  def deleteOffer(id: Int): Future[Int] =
    db.run(offerTable.filter(_.idOffer === id).delete)

   def updateOffer(id: Int, amountMax: Int, price: BigDecimal, dateUpdate: String): Future[Int] =
     db.run(
       offerTable
         .filter(_.idOffer === id)
         .map(x=>(x.amountMax, x.priceMax, x.dateUpdate))
         .update((amountMax, price, Some(dateUpdate))))

  def insertTransaction(transaction: TransactionStock): Future[Option[Int]] = {
    db.run((transactionStockTable returning transactionStockTable.map(_.idTransactionStock)) += transaction)
  }

  def deleteTransaction(id: Int): Future[Int] =
    db.run(transactionStockTable.filter(_.idTransactionStock === id).delete)

  def getTransactionsForOfferSell(idOffer: Int): Future[Seq[TransactionStock]]=
    db.run(
    transactionStockTable
      .filter(_.idOfferSell === idOffer)
      .result)

  def updateTransactionsPrice(idOfferSell: Int, price: BigDecimal): Future[Int] =
    db.run(
      transactionStockTable
        .filter(_.idOfferSell === idOfferSell)
        .map(x=>x.price)
        .update(price))
}
