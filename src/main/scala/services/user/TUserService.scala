package services.user

import models.user.UserModel
import services.TService


trait TUserService[F[_]] extends TService{
  def signIn(login: String, password: String): F[AllErrorsOr[Option[Int]]]

  def create(user: UserModel): F[AllErrorsOr[Boolean]]

  def getUserData: F[AllErrorsOr[Option[UserModel]]]

  def update(user: UserModel): F[AllErrorsOr[Boolean]]
}
