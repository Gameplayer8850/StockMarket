package stock

import cats.implicits.catsSyntaxValidatedIdBinCompat0
import models.DbClasses.{Company, Market, Stock}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import services.stock.StockService

class StockServiceValidationSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks{
  val stockService = new StockService(-1)

  //"companyValidate" tests
  "companyValidate" should "be CompanyInvalid when None" in {
    stockService.StockValidationError.companyValidate(None) shouldBe Left(stockService.StockValidationError.CompanyInvalid)
  }

  it should "be valid when is defined" in {
    val parameter=Some(Company(1, 1, "Full Name", "Short Name", None))
    stockService.StockValidationError.companyValidate(parameter) shouldBe Right(parameter.get)
  }


  //"countryValidate" tests
  "countryValidate" should "be CountryInvalid when None" in {
    stockService.StockValidationError.countryValidate(None) shouldBe Left(stockService.StockValidationError.CountryInvalid)
  }

  it should "be valid when is defined" in {
    val parameter=Some(Market(1, "Location", "Location Short", "Location City", None, None))
    stockService.StockValidationError.countryValidate(parameter) shouldBe Right(parameter.get)
  }


  //"availableBalanceValidate" tests
  "availableBalanceValidate" should "be AvailableBalanceInvalid when available balance is None" in {
    stockService.StockValidationError.availableBalanceValidate(None, amount = 1, price = 10) shouldBe Left(stockService.StockValidationError.AvailableBalanceInvalid)
  }

  it should "be AvailableBalanceInvalid when available balance is lower than cost of offer" in {
    stockService.StockValidationError.availableBalanceValidate(Some(10), amount = 10, price = 2) shouldBe Left(stockService.StockValidationError.AvailableBalanceInvalid)
  }

  it should "be valid when available balance is enough for cost of offer" in {
    stockService.StockValidationError.availableBalanceValidate(Some(20), amount = 10, price = 2) shouldBe Right()
  }


  //"offerValidate" tests
  "offerValidate" should "be OfferInvalid when None" in {
    stockService.StockValidationError.offerValidate(None) shouldBe Left(stockService.StockValidationError.OfferInvalid)
  }

  it should "be OfferInvalid when id <1" in {
    stockService.StockValidationError.offerValidate(Some(0)) shouldBe Left(stockService.StockValidationError.OfferInvalid)
  }

  it should "be valid when correct" in {
    val parameter=Some(2)
    stockService.StockValidationError.offerValidate(parameter) shouldBe Right(parameter.get)
  }


  //"availableStocksValidate" tests
  "availableStocksValidate" should "be AvailableStocksInvalid when list of available stocks is empty" in {
    stockService.StockValidationError.availableStocksValidate(Nil, amount = 1) shouldBe Left(stockService.StockValidationError.AvailableStocksInvalid)
  }

  it should "be AvailableStocksInvalid when amount of available stocks is lower than amount in offer" in {
    stockService.StockValidationError.availableStocksValidate(Seq[Stock](Stock(None, 1, 1, isOnSale = false, "01-01-2012")), amount = 2) shouldBe Left(stockService.StockValidationError.AvailableStocksInvalid)
  }

  it should "be valid when amount of available stocks is bigger (or same) than amount in offer" in {
    val parameter=Seq[Stock](Stock(None, 1, 1, isOnSale = false, "01-01-2012"))
    stockService.StockValidationError.availableStocksValidate(parameter, amount = 1) shouldBe Right(parameter)
  }
}
