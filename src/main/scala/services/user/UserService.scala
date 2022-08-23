package services.user

import cats.syntax.all._
import com.typesafe.scalalogging.LazyLogging
import models.DbClasses.{PersonalData, User}
import models.ValidationErrorModel
import models.user.UserModel
import repositories.user.UserRepository
import services.BasicService

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt
import java.security.MessageDigest
import java.time.{LocalDate, LocalDateTime}
import scala.util.Try

class UserService(idUser: Int) extends BasicService(idUser, "UserService") with TUserService[Future]{
  val userRepository = new UserRepository(dbConnection)

  def generateSHA256(text: String): String = MessageDigest.getInstance("SHA-256")
    .digest(text.getBytes("UTF-8"))
    .map("%02x".format(_)).mkString

  def signIn(login: String, password: String): Future[AllErrorsOr[Option[Int]]]= {
    Future {
      logger.debug(s"Running service called 'signIn' with parameters {login: {$login}, password: {$password}}")
      (UserValidationError.loginValidate(login, exists = false), UserValidationError.passwordValidate(password)).mapN((log, pas) => {
        val passwordSHA256 = generateSHA256(pas)
        Await.result(userRepository.getUserByLoginPassword(log, passwordSHA256), 30.seconds)
          .flatMap(x=>x.idUser)
      })
    }
  }

  def create(user: UserModel): Future[AllErrorsOr[Boolean]] = {
    Future {
      logger.debug(s"Running service called 'getCompanyData' with parameters {user: {$user}}")
      (UserValidationError.loginValidate(user.login, Await.result(userRepository.existsUserWithLogin(user.login, idUser = -1), 30.seconds)), UserValidationError.passwordValidate(user.password.getOrElse("")), UserValidationError.firstNameValidate(user.firstName), UserValidationError.secondNameValidate(user.secondName), UserValidationError.surnameValidate(user.surname), UserValidationError.dateBirthValidate(user.dateBirth)).mapN((login, password, firstName, secondName, surname, dateBirth) => {
        val personal = PersonalData(None, firstName, secondName, surname, dateBirth, user.typeIdentity, user.identity)
        Await.result(userRepository.insertPersonalData(personal), 30.seconds).exists(x => {
          val userData = User(None, user.login, generateSHA256(user.password.get), Option(x), LocalDateTime.now.format(DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm:ss")), None)
          Await.result(userRepository.insertUser(userData), 30.seconds).isDefined
        })
      })
    }
  }

  def getUserData: Future[AllErrorsOr[Option[UserModel]]] = {
    Future {
      logger.debug(s"Running service called 'getUserData'")
      Await.result(userRepository.getUser(idUser), 30.seconds).flatMap(user=>{
        user.idPersonalData.flatMap(idPersonalData=>{
          Await.result(userRepository.getPersonalData(idPersonalData), 30.seconds).flatMap(personal=>{
            Some(UserModel(user.login, None, personal.firstName, personal.secondName, personal.surname, personal.dateBirth, personal.typeIdentity, personal.identity))
          })
        })
      }).validNec
    }
  }

  def update(user: UserModel): Future[AllErrorsOr[Boolean]] = {
    Future {
      logger.debug(s"Running service called 'update' with parameters {user: {$user}}")
      (UserValidationError.loginValidate(user.login, Await.result(userRepository.existsUserWithLogin(user.login, idUser), 30.seconds)),
        if (user.password.isDefined) UserValidationError.passwordValidate(user.password.getOrElse("")) else user.password.validNec,
        UserValidationError.firstNameValidate(user.firstName), UserValidationError.secondNameValidate(user.secondName), UserValidationError.surnameValidate(user.surname), UserValidationError.dateBirthValidate(user.dateBirth)).mapN((login, password, firstName, secondName, surname, dateBirth) => {

        Await.result(userRepository.getUser(idUser), 30.seconds).exists(userMap=>{
          userMap.idPersonalData.exists(idPersonalData=>{
            if(Await.result(userRepository.updatePersonalData(PersonalData(Some(idPersonalData), firstName, secondName, surname, dateBirth, user.typeIdentity, user.identity)), 30.seconds)>0){
              val userData = userMap.copy(login = login, password = user.password.map(passwordMap=>generateSHA256(passwordMap)).getOrElse(userMap.password))
              Await.result(userRepository.updateUser(userData), 30.seconds)>0
            } else false
          })
        })
      })
    }
  }


  sealed trait UserValidationError extends ValidationErrorModel

  object UserValidationError {
    def loginValidate(login: String, exists: Boolean): AllErrorsOr[String] = {
      if (login == null || login.length < 5 || login.exists(_.isSpaceChar)) LoginInvalid.invalidNec
      else if(exists) AvailableLoginInvalid.invalidNec
      else login.validNec
    }

    def passwordValidate(password: String): AllErrorsOr[String] = {
      if (password == null || password.length < 8 || !password.exists(_.isDigit) || !password.exists(_.isUpper) || !password.exists(_.isLower)) PasswordInvalid.invalidNec
      else password.validNec
    }

    def firstNameValidate(firstName: String): AllErrorsOr[String]={
      if(firstName == null || !nameValidate(firstName)) FirstNameInvalid.invalidNec
      else firstName.validNec
    }

    def secondNameValidate(secondName: Option[String]): AllErrorsOr[Option[String]]={
      if(secondName == null || (secondName.isDefined && !nameValidate(secondName.get))) SecondNameInvalid.invalidNec
      else secondName.validNec
    }

    def surnameValidate(surname: String): AllErrorsOr[String]={
      if(surname == null || !nameValidate(surname)) SurnameInvalid.invalidNec
      else surname.validNec
    }

    def dateBirthValidate(dateBirth: String): AllErrorsOr[String]={
      if(dateBirth == null || Try(LocalDate.parse(dateBirth, DateTimeFormatter.ofPattern("yyyy-MM-dd"))).isFailure || ChronoUnit.MONTHS.between(LocalDate.parse(dateBirth, DateTimeFormatter.ofPattern("yyyy-MM-dd")), LocalDate.now)<216) DateBirthInvalid.invalidNec
      else dateBirth.validNec
    }

    private def nameValidate(name: String): Boolean = {
      if (name.length < 3 || name.exists(_.isDigit) || name.exists(_.isSpaceChar) || name.exists(!_.isLetterOrDigit)) false
      else true
    }

    final case object LoginInvalid extends UserValidationError {
      override def errorMessage: String = "Login must be at least 5 characters long."
    }
    final case object AvailableLoginInvalid extends UserValidationError {
      override def errorMessage: String = "Login exists in our system. Please enter a new one."
    }

    final case object PasswordInvalid extends UserValidationError {
      override def errorMessage: String = "Password must be at least 8 characters long, it should contain number, uppercase and lowercase letter."
    }

    final case object FirstNameInvalid extends UserValidationError {
      override def errorMessage: String = "First name must be at least 3 characters long, it cannot contain numbers, spaces and special characters."
    }

    final case object SecondNameInvalid extends UserValidationError {
      override def errorMessage: String = "Second name must be at least 3 characters long, it cannot contain numbers, spaces and special characters."
    }

    final case object SurnameInvalid extends UserValidationError {
      override def errorMessage: String = "Surname must be at least 3 characters long, it cannot contain numbers, spaces and special characters."
    }

    final case object DateBirthInvalid extends UserValidationError {
      override def errorMessage: String = "User must be an adult (18 years or older)."
    }
  }
}

