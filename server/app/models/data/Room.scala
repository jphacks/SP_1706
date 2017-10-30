package models.data

import play.api.data
import play.api.data.Forms._
import util.Extension.FormExtension

/**
  * Created by satou on 2017/10/21.
  */
case class Room(id: Long, url: String, name: String ,password: String)

object Room {

  case class Form(url: String, name: String, password: String)

  object Form extends FormExtension[Form] {
    val url = "url"
    val name = "name"
    val password = "password"

    val form = data.Form(
      mapping(
        url -> text,
        name -> text,
        password -> text
      )(Form.apply)(Form.unapply)
    )
  }

}