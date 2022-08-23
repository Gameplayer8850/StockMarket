package services.transaction

import models.DbClasses.{Offer, Portfolio, TransactionCashDetails, Wallet}
import models.transaction.OfferUpdateModel
import services.TService


trait TTransactionService[F[_]] extends TService{
  def getUserPortfolio: F[AllErrorsOr[List[Portfolio]]]

  def getUserWallet: F[AllErrorsOr[Option[Wallet]]]

  def getUserTransactions: F[AllErrorsOr[List[TransactionCashDetails]]]

  def getUserOffersSell: F[AllErrorsOr[List[Offer]]]

  def getUserOffersBuy: F[AllErrorsOr[List[Offer]]]

  def updateOffer(idOffer: Int, typeOffer: String, operation: OfferUpdateModel): F[AllErrorsOr[Boolean]]

  def deleteOffer(idOffer: Int, typeOffer: String): F[AllErrorsOr[Boolean]]
}
