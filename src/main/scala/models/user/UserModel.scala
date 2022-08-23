package models.user

case class UserModel(login: String, password: Option[String], firstName: String, secondName: Option[String], surname: String, dateBirth: String, typeIdentity: Option[String], identity: Option[String])
