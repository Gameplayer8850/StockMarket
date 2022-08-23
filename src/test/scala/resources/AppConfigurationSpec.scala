package resources

import com.typesafe.config.ConfigFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class AppConfigurationSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks{

  def checkIsEmpty(path: String): Boolean =
    ConfigFactory.load().isEmpty || !ConfigFactory.load().hasPath(path) || ConfigFactory.load().getString(path).trim.isEmpty

  def checkIsNumber(path: String): Boolean =
    ConfigFactory.load().getString(path).forall(Character.isDigit)


  //"sqlserver" tests
  "sqlserver" should "not have empty 'host'" in {
    checkIsEmpty("configuration.sqlserver.host") shouldBe false
  }

  it should "not have empty 'databaseName'" in {
    checkIsEmpty("configuration.sqlserver.databaseName") shouldBe false
  }


  //"apiServer" tests
  "apiServer" should "not have empty 'interface'" in {
    checkIsEmpty("configuration.apiServer.interface") shouldBe false
  }

  it should "not have empty 'port'" in {
    checkIsEmpty("configuration.apiServer.port") shouldBe false
  }

  it should "not have empty 'prefix'" in {
    checkIsEmpty("configuration.apiServer.prefix") shouldBe false
  }

  it should "not have empty 'errorMessage'" in {
    checkIsEmpty("configuration.apiServer.errorMessage") shouldBe false
  }


  //"oAuth" tests
  "oAuth" should "not have empty 'expirationTimeMillis'" in {
    checkIsEmpty("configuration.oAuth.expirationTimeMillis") || !checkIsNumber("configuration.oAuth.expirationTimeMillis")  shouldBe false
  }

  it should "not have empty 'deleteInitialDelayMillis'" in {
    checkIsEmpty("configuration.oAuth.deleteInitialDelayMillis") || !checkIsNumber("configuration.oAuth.deleteInitialDelayMillis")  shouldBe false
  }

  it should "not have empty 'deleteDelayMillis'" in {
    checkIsEmpty("configuration.oAuth.deleteDelayMillis") || !checkIsNumber("configuration.oAuth.deleteDelayMillis")  shouldBe false
  }


  //"matchTransaction" tests
  "matchTransaction" should "not have empty 'execInitialDelayMillis'" in {
    checkIsEmpty("configuration.matchTransaction.execInitialDelayMillis") || !checkIsNumber("configuration.matchTransaction.execInitialDelayMillis")  shouldBe false
  }

  it should "not have empty 'execDelayMillis'" in {
    checkIsEmpty("configuration.matchTransaction.execDelayMillis") || !checkIsNumber("configuration.matchTransaction.execDelayMillis")  shouldBe false
  }
}
