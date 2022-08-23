package services.transaction

import cats.syntax.all._
import models.DbClasses.{Offer, Portfolio, Stock, TransactionCashDetails, TransactionStock, Wallet}
import models.ValidationErrorModel
import models.transaction.OfferUpdateModel
import repositories.transaction.TransactionRepository
import services.BasicService
import services.stock.StockService

import java.time.format.DateTimeFormatter
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

class TransactionService(idUser: Int) extends BasicService(idUser, "TransactionService") with TTransactionService[Future]{
  val transactionRepository = new TransactionRepository(dbConnection)

  def getUserPortfolio: Future[AllErrorsOr[List[Portfolio]]] ={
    for {
      _ <- log(s"Running service called 'getUserPortfolio'")
      portfolio <- transactionRepository.getUserPortfolio(idUser)
    }yield(portfolio.toList.validNec)
  }

  def getUserWallet: Future[AllErrorsOr[Option[Wallet]]] ={
    for {
      _ <- log(s"Running service called 'getUserWallet'")
      wallet <- transactionRepository.getUserWallet(idUser)
    }yield(wallet.validNec)
  }

  def getUserTransactions: Future[AllErrorsOr[List[TransactionCashDetails]]] ={
    for {
      _ <- log(s"Running service called 'getUserTransactions'")
      transactions <- transactionRepository.getUserTransactions(idUser)
    }yield(transactions.toList.validNec)
  }

  def getUserOffersSell: Future[AllErrorsOr[List[Offer]]] ={
    for {
      _ <- log(s"Running service called 'getUserOffersSell'")
      offers <- transactionRepository.getUserOffers(idUser, "S")
    }yield(offers.toList.validNec)
  }

  def getUserOffersBuy: Future[AllErrorsOr[List[Offer]]] ={
    for {
      _ <- log(s"Running service called 'getUserOffersBuy'")
      offers <- transactionRepository.getUserOffers(idUser, "B")
    }yield(offers.toList.validNec)
  }

  def updateOffer(idOffer: Int, typeOffer: String, operation: OfferUpdateModel): Future[AllErrorsOr[Boolean]]={
    Future {
      logger.debug(s"Running service called 'updateOffer' with parameters {idOffer: {$idOffer}, typeOffer: {$typeOffer}, operation: {$operation}}")
      val stockService = new StockService(idUser)
      TransactionValidationError.offerValidate(Await.result(transactionRepository.getOffer(idOffer), 30.seconds), idUser, typeOffer) match {
        case Left(offerError) => offerError.invalidNec
        case Right(offer) =>
          TransactionValidationError.operationValidate(operation, offer) match {
            case Left(operationError) => operationError.invalidNec
            case _ => {
              val price = operation.price.getOrElse(offer.priceMax)
              if (typeOffer == "B") {
                //buy
                TransactionValidationError.availableBalanceValidate(getUserWalletAvailableBalance, offer, operation) match {
                  case Left(availableBalanceError) => availableBalanceError.invalidNec
                  case _ => {
                    Await.result(transactionRepository.updateOffer(idOffer, operation.amountMax, price, java.time.LocalDateTime.now.format(DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm:ss"))), 30.seconds)
                    true.validNec
                  }
                }
              } else {
                //sell
                val amountStatus:Either[ValidationErrorModel, Unit]= {
                  if (operation.amountMax > offer.amountMax) {
                    TransactionValidationError.availableStocksValidate(stockService.getStockAvailableToSale(offer.idCompany), offer, operation) match {
                      case Left(aStocksError) => Left(aStocksError)
                      case Right(stocks) => Right(
                        for (stock <- stocks.take(operation.amountMax - offer.amountMax)) {
                          stockService.addStocksToTransaction(idOffer, price, stock)
                        })
                    }
                  } else if (operation.amountMax < offer.amountMax) {
                    TransactionValidationError.transactionsValidate(Await.result(transactionRepository.getTransactionsForOfferSell(idOffer), 30.seconds)) match {
                      case Left(transactionsError) => Left(transactionsError)
                      case Right(transactions) =>Right(
                        for (transaction <- transactions.take(offer.amountMax - operation.amountMax)) {
                          stockService.updateStatusStock(transaction.idStock, isOnSale = false)
                          Await.result(transactionRepository.deleteTransaction(transaction.idTransactionStock.get), 30.seconds)
                        })
                    }
                  } else Right()
                }

                amountStatus match{
                  case Left(resError) => resError.invalidNec
                  case _ => {
                    Await.result(transactionRepository.updateOffer(idOffer, operation.amountMax, price, java.time.LocalDateTime.now.format(DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm:ss"))), 30.seconds)
                    if (price != offer.priceMax) Await.result(transactionRepository.updateTransactionsPrice(idOffer, price), 30.seconds)
                    true.validNec
                  }
                }
              }
            }
          }
      }
    }
  }

  def deleteOffer(idOffer: Int, typeOffer: String): Future[AllErrorsOr[Boolean]]={
    Future {
      logger.debug(s"Running service called 'deleteOffer' with parameters {idOffer: {$idOffer}, typeOffer: {$typeOffer}}")
      val stockService = new StockService(idUser)
      TransactionValidationError.offerValidate(Await.result(transactionRepository.getOffer(idOffer), 30.seconds), idUser, typeOffer) match {
        case Left(offerError) => offerError.invalidNec
        case Right(offer) =>
          TransactionValidationError.offerAmountCurrentValidate(offer) match {
            case Left(offerAmountCurrentError) => offerAmountCurrentError.invalidNec
            case _ => {
              if (typeOffer == "S") {
                TransactionValidationError.transactionsValidate(Await.result(transactionRepository.getTransactionsForOfferSell(idOffer), 30.seconds)) match {
                  case Left(transactionsError) => transactionsError.invalidNec
                  case Right(transactions) =>
                    for (transaction <- transactions) {
                      stockService.updateStatusStock(transaction.idStock, isOnSale = false)
                      Await.result(transactionRepository.deleteTransaction(transaction.idTransactionStock.get), 30.seconds)
                    }
                    Await.result(transactionRepository.deleteOffer(idOffer), 30.seconds)
                    true.validNec
                }
              } else{
                Await.result(transactionRepository.deleteOffer(idOffer), 30.seconds)
                true.validNec
              }
            }
          }
      }
    }
  }

  def getUserWalletAvailableBalance: Option[BigDecimal] ={
    Await.result(transactionRepository.getUserWallet(idUser), 30.seconds).map(x=>x.availableBalance)
  }

  def createOffer(typeOffer: String, idCompany: Int, amountMax : Int, priceMax: BigDecimal): Option[Int]= {
    val offer = Offer(None, typeOffer, idCompany, idUser, amountMax, 0, priceMax, java.time.LocalDateTime.now.format(DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm:ss")), None)
    Await.result(transactionRepository.insertOffer(offer), 30.seconds)
  }

  def createTransaction(idOfferSell: Int, idStock: Int, price: BigDecimal): Boolean= {
    val transaction = TransactionStock(None, idStock, price, idUser, None, idOfferSell, None, java.time.LocalDateTime.now.format(DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm:ss")), None)
    Await.result(transactionRepository.insertTransaction(transaction), 30.seconds).isDefined
  }

  sealed trait TransactionValidationError extends ValidationErrorModel

  object TransactionValidationError {
    def offerValidate(offer: Option[Offer], idUser: Int, typeOffer: String): Either[TransactionValidationError, Offer] = {
      offer.flatMap(x=>if(x.idUser!=idUser || x.typeOffer!=typeOffer) None else Option(Right(x)))
        .getOrElse(Left(OfferInvalid))
    }

    def offerAmountCurrentValidate(offer: Offer): Either[TransactionValidationError, Unit] = {
      if(offer.amountCurrent>0) Left(OfferAmountCurrentInvalid)
      else Right()
    }

    def operationValidate(operation: OfferUpdateModel, offer: Offer): Either[TransactionValidationError, Unit] = {
      if(operation.amountMax<offer.amountCurrent || (operation.price.isDefined && offer.amountCurrent>0)) Left(OperationInvalid)
      else Right()
    }

    def availableBalanceValidate(availableBalance: Option[BigDecimal], offer: Offer, operation: OfferUpdateModel): Either[TransactionValidationError, BigDecimal] = {
      availableBalance.flatMap(x=>{
        val availableBalanceWithReserved = x+(offer.priceMax * (offer.amountMax-offer.amountCurrent))
        if(availableBalanceWithReserved - (operation.price.getOrElse(offer.priceMax)*(operation.amountMax-offer.amountCurrent))<0) None else Option(Right(x))
      }).getOrElse(Left(AvailableBalanceInvalid))
    }

    def availableStocksValidate(stocks: Seq[Stock], offer: Offer, operation: OfferUpdateModel): Either[TransactionValidationError, Seq[Stock]] = {
      if(stocks.isEmpty || stocks.length<operation.amountMax-offer.amountMax) Left(AvailableStocksInvalid)
      else Right(stocks)
    }

    def transactionsValidate(transactions: Seq[TransactionStock]): Either[TransactionValidationError, Seq[TransactionStock]] = {
      if(transactions.isEmpty) Left(TransactionsInvalid)
      else Right(transactions)
    }

    final case object OfferInvalid extends TransactionValidationError {
      override def errorMessage: String = "Invalid offer with given id."
    }

    final case object OfferAmountCurrentInvalid extends TransactionValidationError {
      override def errorMessage: String = "You cannot delete offer in progress "
    }

    final case object OperationInvalid extends TransactionValidationError {
      override def errorMessage: String = "Invalid operation data for a given offer. You cannot change price for offer in progress and set amount lower than already sold."
    }

    final case object AvailableBalanceInvalid extends TransactionValidationError {
      override def errorMessage: String = "You cannot submit this offer with available balance."
    }

    final case object AvailableStocksInvalid extends TransactionValidationError {
      override def errorMessage: String = "You cannot submit this offer with your stocks portfolio."
    }

    final case object TransactionsInvalid extends TransactionValidationError {
      override def errorMessage: String = "System was not able to find transactions for this offer."
    }
  }
}
