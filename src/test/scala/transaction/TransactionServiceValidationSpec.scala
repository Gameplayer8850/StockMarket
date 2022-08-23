package transaction

import models.DbClasses.{Offer, Stock, TransactionStock}
import models.transaction.OfferUpdateModel
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import services.transaction.TransactionService

class TransactionServiceValidationSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks{
  val transactionService = new TransactionService(-1)

  //"offerValidate" tests
  "offerValidate" should "be OfferInvalid when None" in {
    transactionService.TransactionValidationError.offerValidate(None, 1, "B") shouldBe Left(transactionService.TransactionValidationError.OfferInvalid)
  }

  it should "be OfferInvalid when offer was created by someone else" in {
    transactionService.TransactionValidationError.offerValidate(Some(Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 1, amountCurrent = 1, priceMax = 1, "01-01-2012", None)), 2, "S") shouldBe Left(transactionService.TransactionValidationError.OfferInvalid)
  }

  it should "be OfferInvalid when offer is another type (for offer was to sale, but you are trying to modify buy)" in {
    transactionService.TransactionValidationError.offerValidate(Some(Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 1, amountCurrent = 1, priceMax = 1, "01-01-2012", None)), 1, "B") shouldBe Left(transactionService.TransactionValidationError.OfferInvalid)
  }

  it should "be valid when correct" in {
    val parameter = Some(Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 1, amountCurrent = 1, priceMax = 1, "01-01-2012", None))
    transactionService.TransactionValidationError.offerValidate(parameter, 1, "S") shouldBe Right(parameter.get)
  }


  //"offerAmountCurrentValidate" tests
  "offerAmountCurrentValidate" should "be OfferAmountCurrentInvalid when is bigger than 0" in {
    transactionService.TransactionValidationError.offerAmountCurrentValidate(Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 1, amountCurrent = 1, priceMax = 1, "01-01-2012", None)) shouldBe Left(transactionService.TransactionValidationError.OfferAmountCurrentInvalid)
  }

  it should "be valid when is 0" in {
    transactionService.TransactionValidationError.offerAmountCurrentValidate(Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 1, amountCurrent = 0, priceMax = 1, "01-01-2012", None)) shouldBe Right()
  }


  //"operationValidate" tests
  "operationValidate" should "be OperationInvalid when amount in operation is lower than offer current amount" in {
    transactionService.TransactionValidationError.operationValidate(OfferUpdateModel(1, None), Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 3, amountCurrent = 2, priceMax = 1, "01-01-2012", None)) shouldBe Left(transactionService.TransactionValidationError.OperationInvalid)
  }

  it should "be OperationInvalid when operation is trying to change price in offer in progress (amountCurrent>0)" in {
    transactionService.TransactionValidationError.operationValidate(OfferUpdateModel(2, Some(10)), Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 3, amountCurrent = 2, priceMax = 1, "01-01-2012", None)) shouldBe Left(transactionService.TransactionValidationError.OperationInvalid)
  }

  it should "be valid when operation price is None and amount is correct" in {
    transactionService.TransactionValidationError.operationValidate(OfferUpdateModel(3, None), Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 3, amountCurrent = 2, priceMax = 1, "01-01-2012", None)) shouldBe Right()
  }

  it should "be valid when operation price is defined and offer is not in progress (amountCurrent==0)" in {
    transactionService.TransactionValidationError.operationValidate(OfferUpdateModel(3, Some(10)), Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 3, amountCurrent = 0, priceMax = 1, "01-01-2012", None)) shouldBe Right()
  }


  //"availableBalanceValidate" tests
  "availableBalanceValidate" should "be AvailableBalanceInvalid when available balance is None" in {
    transactionService.TransactionValidationError.availableBalanceValidate(None, Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 3, amountCurrent = 2, priceMax = 1, "01-01-2012", None), OfferUpdateModel(3, None)) shouldBe Left(transactionService.TransactionValidationError.AvailableBalanceInvalid)
  }

  it should "be AvailableBalanceInvalid when available balance after updating offer (by changing amount) will be lower than 0" in {
    transactionService.TransactionValidationError.availableBalanceValidate(Some(10), Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 3, amountCurrent = 2, priceMax = 5, "01-01-2012", None), OfferUpdateModel(6, None)) shouldBe Left(transactionService.TransactionValidationError.AvailableBalanceInvalid)
  }

  it should "be AvailableBalanceInvalid when available balance after updating offer (by changing price) will be lower than 0" in {
    transactionService.TransactionValidationError.availableBalanceValidate(Some(10), Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 3, amountCurrent = 0, priceMax = 5, "01-01-2012", None), OfferUpdateModel(3, Some(10))) shouldBe Left(transactionService.TransactionValidationError.AvailableBalanceInvalid)
  }

  it should "be valid when correct amount (with None price)" in {
    val parameter = Some(10)
    transactionService.TransactionValidationError.availableBalanceValidate(Some(10), Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 3, amountCurrent = 2, priceMax = 5, "01-01-2012", None), OfferUpdateModel(4, None)) shouldBe Right(parameter.get)
  }

  it should "be valid when correct price (with old amount)" in {
    val parameter = Some(10)
    transactionService.TransactionValidationError.availableBalanceValidate(Some(10), Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 3, amountCurrent = 0, priceMax = 5, "01-01-2012", None), OfferUpdateModel(3, Some(6))) shouldBe Right(parameter.get)
  }

  it should "be valid when correct amount and price" in {
    val parameter = Some(10)
    transactionService.TransactionValidationError.availableBalanceValidate(Some(10), Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 3, amountCurrent = 0, priceMax = 5, "01-01-2012", None), OfferUpdateModel(4, Some(6))) shouldBe Right(parameter.get)
  }


  //"availableStocksValidate" tests
  "availableStocksValidate" should "be AvailableStocksInvalid when list of available stocks is empty" in {
    transactionService.TransactionValidationError.availableStocksValidate(Nil, Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 3, amountCurrent = 2, priceMax = 1, "01-01-2012", None), OfferUpdateModel(3, None)) shouldBe Left(transactionService.TransactionValidationError.AvailableStocksInvalid)
  }

  it should "be AvailableStocksInvalid when amount of available stocks is lower than amount in offer" in {
    transactionService.TransactionValidationError.availableStocksValidate(Seq[Stock](Stock(None, 1, 1, isOnSale = false, "01-01-2012")), Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 3, amountCurrent = 2, priceMax = 1, "01-01-2012", None), OfferUpdateModel(5, None)) shouldBe Left(transactionService.TransactionValidationError.AvailableStocksInvalid)
  }

  it should "be valid when correct" in {
    val parameter = Seq[Stock](Stock(None, 1, 1, isOnSale = false, "01-01-2012"))
    transactionService.TransactionValidationError.availableStocksValidate(parameter, Offer(None, "S", idCompany = 1, idUser = 1, amountMax = 3, amountCurrent = 2, priceMax = 1, "01-01-2012", None), OfferUpdateModel(4, None)) shouldBe Right(parameter)
  }


  //"transactionsValidate" tests
  "transactionsValidate" should "be TransactionsInvalid when list of transactions is empty" in {
    transactionService.TransactionValidationError.transactionsValidate(Nil) shouldBe Left(transactionService.TransactionValidationError.TransactionsInvalid)
  }

  it should "be valid when correct" in {
    val parameter = Seq[TransactionStock](TransactionStock(None, idStock = 1, price = 1, idSeller = 1, idBuyer = None, idOfferSell = 1, idOfferBuy = None, "01-01-2012", None))
    transactionService.TransactionValidationError.transactionsValidate(parameter) shouldBe Right(parameter)
  }
}
