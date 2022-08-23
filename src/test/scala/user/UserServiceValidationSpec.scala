package user
import cats.implicits.catsSyntaxValidatedIdBinCompat0
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import services.user.UserService

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UserServiceValidationSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks{
  val userService = new UserService(-1)

  //"loginValidate" tests
  "loginValidate" should "be LoginInvalid when null" in {
    userService.UserValidationError.loginValidate(login = null, exists = false) shouldBe userService.UserValidationError.LoginInvalid.invalidNec
  }

  it should "be LoginInvalid when length < 5" in {
    userService.UserValidationError.loginValidate(login = "Tes1", exists = false) shouldBe userService.UserValidationError.LoginInvalid.invalidNec
  }

  it should "be LoginInvalid when contains space char" in {
    userService.UserValidationError.loginValidate(login = "Test 123", exists = false) shouldBe userService.UserValidationError.LoginInvalid.invalidNec
  }

  it should "be AvailableLoginInvalid when 'exists' parameter is true" in {
    userService.UserValidationError.loginValidate(login = "Test123", exists = true) shouldBe userService.UserValidationError.AvailableLoginInvalid.invalidNec
  }

  it should "be valid when correct" in {
    val parameter="Test123"
    userService.UserValidationError.loginValidate(login = parameter, exists = false) shouldBe parameter.validNec
  }


  //"passwordValidate" tests
  "passwordValidate" should "be PasswordInvalid when null" in {
    userService.UserValidationError.passwordValidate(password = null) shouldBe userService.UserValidationError.PasswordInvalid.invalidNec
  }

  it should "be PasswordInvalid when when length < 8" in {
    userService.UserValidationError.passwordValidate(password = "Passwo1") shouldBe userService.UserValidationError.PasswordInvalid.invalidNec
  }

  it should "be PasswordInvalid when when not contain digit" in {
    userService.UserValidationError.passwordValidate(password = "Password") shouldBe userService.UserValidationError.PasswordInvalid.invalidNec
  }

  it should "be PasswordInvalid when when not contain upper letter" in {
    userService.UserValidationError.passwordValidate(password = "password1") shouldBe userService.UserValidationError.PasswordInvalid.invalidNec
  }

  it should "be PasswordInvalid when when not contain lower letter" in {
    userService.UserValidationError.passwordValidate(password = "PASSWORD1") shouldBe userService.UserValidationError.PasswordInvalid.invalidNec
  }

  it should "be valid when when correct" in {
    val parameter="Password1"
    userService.UserValidationError.passwordValidate(password = parameter) shouldBe parameter.validNec
  }


  //"firstNameValidate" tests
  "firstNameValidate" should "be FirstNameInvalid when null" in {
    userService.UserValidationError.firstNameValidate(firstName = null) shouldBe userService.UserValidationError.FirstNameInvalid.invalidNec
  }

  it should "be FirstNameInvalid when length < 3" in {
    userService.UserValidationError.firstNameValidate(firstName = "Ma") shouldBe userService.UserValidationError.FirstNameInvalid.invalidNec
  }

  it should "be FirstNameInvalid when contain Digit" in {
    userService.UserValidationError.firstNameValidate(firstName = "Marek12") shouldBe userService.UserValidationError.FirstNameInvalid.invalidNec
  }

  it should "be FirstNameInvalid when contain space char" in {
    userService.UserValidationError.firstNameValidate(firstName = "Marek 12") shouldBe userService.UserValidationError.FirstNameInvalid.invalidNec
  }

  it should "be FirstNameInvalid when contain special character" in {
    userService.UserValidationError.firstNameValidate(firstName = "Marek#12") shouldBe userService.UserValidationError.FirstNameInvalid.invalidNec
  }

  it should "be valid when correct" in {
    val parameter="Marek"
    userService.UserValidationError.firstNameValidate(firstName = parameter) shouldBe parameter.validNec
  }


  //"secondNameValidate" tests
  "secondNameValidate" should "be SecondNameInvalid when null" in {
    userService.UserValidationError.secondNameValidate(secondName = null) shouldBe userService.UserValidationError.SecondNameInvalid.invalidNec
  }

  it should "be SecondNameInvalid when is defined and length < 3" in {
    userService.UserValidationError.secondNameValidate(secondName = Some("Ma")) shouldBe userService.UserValidationError.SecondNameInvalid.invalidNec
  }

  it should "be SecondNameInvalid when is defined and contain Digit" in {
    userService.UserValidationError.secondNameValidate(secondName = Some("Marek12")) shouldBe userService.UserValidationError.SecondNameInvalid.invalidNec
  }

  it should "be SecondNameInvalid when is defined and contain space char" in {
    userService.UserValidationError.secondNameValidate(secondName = Some("Marek 12")) shouldBe userService.UserValidationError.SecondNameInvalid.invalidNec
  }

  it should "be SecondNameInvalid when is defined and contain special character" in {
    userService.UserValidationError.secondNameValidate(secondName = Some("Marek#12")) shouldBe userService.UserValidationError.SecondNameInvalid.invalidNec
  }

  it should "be valid when is defined and correct" in {
    val parameter=Some("Marek")
    userService.UserValidationError.secondNameValidate(secondName = parameter) shouldBe parameter.validNec
  }

  it should "be valid when is not defined" in {
    val parameter=None
    userService.UserValidationError.secondNameValidate(secondName = parameter) shouldBe parameter.validNec
  }


  //"surnameValidate" tests
  "surnameValidate" should "be SurnameInvalid when null" in {
    userService.UserValidationError.surnameValidate(surname = null) shouldBe userService.UserValidationError.SurnameInvalid.invalidNec
  }

  it should "be SurnameInvalid when length < 3" in {
    userService.UserValidationError.surnameValidate(surname = "Su") shouldBe userService.UserValidationError.SurnameInvalid.invalidNec
  }

  it should "be SurnameInvalid when contain Digit" in {
    userService.UserValidationError.surnameValidate(surname = "Surname12") shouldBe userService.UserValidationError.SurnameInvalid.invalidNec
  }

  it should "be SurnameInvalid when contain space char" in {
    userService.UserValidationError.surnameValidate(surname = "Surname 12") shouldBe userService.UserValidationError.SurnameInvalid.invalidNec
  }

  it should "be SurnameInvalid when contain special character" in {
    userService.UserValidationError.surnameValidate(surname = "Surname#12") shouldBe userService.UserValidationError.SurnameInvalid.invalidNec
  }

  it should "be valid when correct" in {
    val parameter="Surname"
    userService.UserValidationError.surnameValidate(surname = parameter) shouldBe parameter.validNec
  }


  //"dateBirthValidate" tests
  "dateBirthValidate" should "be DateBirthInvalid when null" in {
    userService.UserValidationError.dateBirthValidate(dateBirth = null) shouldBe userService.UserValidationError.DateBirthInvalid.invalidNec
  }

  it should "be DateBirthInvalid when invalid format" in {
    userService.UserValidationError.dateBirthValidate(dateBirth = "01/12/1900") shouldBe userService.UserValidationError.DateBirthInvalid.invalidNec
  }

  it should "be DateBirthInvalid when not adult (18+)" in {
    userService.UserValidationError.dateBirthValidate(dateBirth = LocalDate.now.minusMonths(215).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) shouldBe userService.UserValidationError.DateBirthInvalid.invalidNec
  }

  it should "be valid when correct and adult" in {
    val parameter=LocalDate.now.minusMonths(216).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    userService.UserValidationError.dateBirthValidate(dateBirth = parameter) shouldBe parameter.validNec
  }
}
