package services

import cats.data.ValidatedNec
import models.ValidationErrorModel

trait TService {
  type AllErrorsOr[A] = ValidatedNec[ValidationErrorModel, A]
}
