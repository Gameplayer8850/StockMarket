package repositories.transaction

import models.DbClasses.{Offer, Portfolio, TransactionCashDetails, TransactionStock, Wallet}


trait TTransactionRepository[F[_]] {
  def getUserPortfolio(idUser: Int): F[Seq[Portfolio]]

  def getUserOffers(idUser: Int, typeOffer: String): F[Seq[Offer]]

  def getUserWallet(idUser: Int): F[Option[Wallet]]

  def getUserTransactions(idUser: Int): F[Seq[TransactionCashDetails]]

  def getOffer(id: Int): F[Option[Offer]]

  def insertOffer(offer: Offer): F[Option[Int]]

  def deleteOffer(id: Int): F[Int]

  def updateOffer(id: Int, amountMax: Int, price: BigDecimal, dateUpdate: String): F[Int]

  def insertTransaction(transaction: TransactionStock): F[Option[Int]]

  def deleteTransaction(id: Int): F[Int]

  def getTransactionsForOfferSell(idOffer: Int): F[Seq[TransactionStock]]

  def updateTransactionsPrice(idOfferSell: Int, price: BigDecimal): F[Int]
}
