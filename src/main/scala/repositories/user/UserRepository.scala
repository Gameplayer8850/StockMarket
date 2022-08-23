package repositories.user

import models.DbClasses.{PersonalData, User}
import repositories.DbTables.{PersonalDataTable, UserTable}
import slick.jdbc.SQLServerProfile
import slick.jdbc.SQLServerProfile.api._

import scala.concurrent.Future


class UserRepository(db: SQLServerProfile.backend.DatabaseDef) extends TUserRepository[Future]{
  private lazy val userTable= TableQuery[UserTable]
  private lazy val personalDataTable= TableQuery[PersonalDataTable]

  def getUser(id: Int): Future[Option[User]] =
    db.run(
      userTable
        .filter(_.idUser === id)
        .take(1)
        .result
        .headOption)

  def insertUser(user: User): Future[Option[Int]] = {
    db.run((userTable returning userTable.map(_.idUser)) += user)
  }

  def updateUser(user: User): Future[Int] =
    db.run(
      userTable
        .filter(_.idUser === user.idUser)
        .map(x=>(x.login, x.password))
        .update((user.login, user.password)))

  def deleteUser(id: Int): Future[Int] =
    db.run(userTable.filter(_.idUser === id).delete)

  def getPersonalData(id: Int): Future[Option[PersonalData]] =
    db.run(
      personalDataTable
        .filter(_.idPersonalData === id)
        .take(1)
        .result
        .headOption)

  def insertPersonalData(personal: PersonalData): Future[Option[Int]] = {
    db.run((personalDataTable returning personalDataTable.map(_.idPersonalData)) += personal)
  }

  def updatePersonalData(personal: PersonalData): Future[Int] =
    db.run(
      personalDataTable
        .filter(_.idPersonalData === personal.idPersonalData).map(x=>(x.firstName, x.secondName, x.surname, x.dateBirth, x.typeIdentity, x.identity))
        .update((personal.firstName, personal.secondName, personal.surname, personal.dateBirth, personal.typeIdentity, personal.identity)))

  def deletePersonalData(id: Int): Future[Int] =
    db.run(personalDataTable.filter(_.idPersonalData === id).delete)

//custom query
  def getUserByLoginPassword(login: String, password: String): Future[Option[User]] =
    db.run(
      userTable
        .filter(x=>x.login===login && x.password===password && x.dateClose.isEmpty)
        .take(1)
        .result
        .headOption)

  def existsUserWithLogin(login: String, idUser: Int): Future[Boolean] =
    db.run(
      userTable
        .filter(x=>x.login===login && x.idUser=!=idUser && x.dateClose.isEmpty)
        .exists
        .result
    )

}
