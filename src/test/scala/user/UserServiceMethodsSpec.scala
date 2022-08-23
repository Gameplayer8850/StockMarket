package user

import cats.implicits.catsSyntaxValidatedIdBinCompat0
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import services.user.UserService


class UserServiceMethodsSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks{
  val userService = new UserService(-1)

  //"generateSHA256" tests
  "generateSHA256" should "be implemented correctly" in {
    userService.generateSHA256(text="Ex@mple123Text") shouldBe "f65a7ac8a873a77dec678cc43559bbab96a8d8044cba9ed1de87ab5c4c264a32"
  }

  /*
  //"signIn" tests
  "signIn" should "be LoginInvalid when only login is invalid" in {
    userService.signIn(login = "123", password = "Password1").futureValue  shouldBe userService.UserValidationError.LoginInvalid.invalidNec
  }

  it should "be LoginInvalid when only password is invalid" in {
    userService.signIn(login = "Login123", password = "123").futureValue shouldBe userService.UserValidationError.PasswordInvalid.invalidNec
  }

  it should "be LoginInvalid when login and password is invalid" in {
    userService.signIn(login = "123", password = "123").futureValue shouldBe (userService.UserValidationError.LoginInvalid, userService.UserValidationError.PasswordInvalid).invalidNec
  }
  */
}
