package models.data

import play.api.data.Forms._
import play.api.data
import play.api.data.format.Formats._
import play.api.libs.json.Json
import util.Extension.{AuthenticatableForm, FormExtension}

/**
  * Created by satou on 2017/10/21.
  */
case class Chat(id: Long, roomId: Long, no: Int, userId: Long,
                timeStamp: String, replyTo: Option[Int], content: String) {
  def toResult(userIdOpt: Option[Long] = None): Chat.Result = Chat.Result(
    no,
    timeStamp,
    replyTo,
    content,
    userIdOpt.fold(false)(userId.==)
  )
}

object Chat {

  implicit def jsonWrites = Json.writes[Chat]

  case class PostForm(roomId: Long, userId: Long, content: String) extends AuthenticatableForm

  object PostForm extends FormExtension[PostForm] {
    val roomId = "roomId"
    val userId = "userId"
    val content = "content"

    val form = data.Form(
      mapping(
        roomId -> of[Long],
        userId -> of[Long],
        content -> text
      )(PostForm.apply)(PostForm.unapply)
    )
  }

  case class AjaxForm(roomId: Long, userId: Long ,no: Int) extends AuthenticatableForm

  object AjaxForm extends FormExtension[AjaxForm] {
    val roomId = "roomId"
    val userId = "userId"
    val no = "no"

    val form = data.Form(
      mapping(
        roomId -> of[Long],
        userId -> of[Long],
        no -> number
      )(AjaxForm.apply)(AjaxForm.unapply)
    )
  }

  case class Result(no: Int, timeStamp: String, replyTo: Option[Int], content: String, isSelf: Boolean)

  object Result {
    implicit def jsonWrites = Json.writes[Result]
  }

}