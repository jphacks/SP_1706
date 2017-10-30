package models.data

import play.api.data
import play.api.data.Forms._
import util.Extension.FormExtension

/**
  * Created by satou on 2017/10/22.
  */
case class LoginForm(url: String, password: String)

object LoginForm extends FormExtension[LoginForm] {

  val url = "url"
  val password = "password"

  val form = data.Form(
    mapping(
      url -> text,
      password -> text
    )(LoginForm.apply)(LoginForm.unapply)
  )
}