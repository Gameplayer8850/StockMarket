package repositories.user

import models.DbClasses.{PersonalData, User}


trait TUserRepository [F[_]]{
  def getUser(id: Int): F[Option[User]]

  def insertUser(user: User): F[Option[Int]]

  def updateUser(user: User): F[Int]

  def deleteUser(id: Int): F[Int]

  def getPersonalData(id: Int): F[Option[PersonalData]]

  def insertPersonalData(personal: PersonalData): F[Option[Int]]

  def updatePersonalData(personal: PersonalData): F[Int]

  def deletePersonalData(id: Int): F[Int]

  def getUserByLoginPassword(login: String, password: String): F[Option[User]]

  def existsUserWithLogin(login: String, idUser: Int): F[Boolean]
}
