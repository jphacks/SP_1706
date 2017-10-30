package models.data

import play.api.data
import play.api.data.Forms._
import play.api.mvc.{Controller, Request, Result}
import services.{RoomService, UserService}
import util.Extension.{AuthenticatableForm, FormExtension}

import scala.util.Try

/**
  * Created by satou on 2017/10/21.
  */
case class User(id: Long, roomId: Long, nickName: String)

object User {
  def sessionName(roomId: Long): String = s"pseudcussion-$roomId"
  private val regex = """pseudcussion-(\d+)""".r
  def getRoomIdFromSessionName(sessionName: String): Option[Long] = Try {
    regex.findFirstIn(sessionName).map(_.toLong)
  }.toOption.flatten

  def authenticate[A](roomId: Long)(ifSuccess: User => A)(ifFailed: => A)(implicit request: Request[_]): A =
    request.session
      .get(sessionName(roomId))
      .flatMap(id => Try(id.toLong).toOption)
      .flatMap(UserService.findById)            // Userが存在しているかどうかの確認
      .filter(_.roomId == roomId)               // UserがRoomに所属しているかどうかの確認
      .fold(ifFailed)(ifSuccess)

  def authenticate[A](form: AuthenticatableForm)(roomId: Long)(ifSuccess: User => A)(ifFailed: => A)(implicit request: Request[_]): A =
    request.session
      .get(sessionName(roomId))
      .flatMap(id => Try(id.toLong).toOption)
      .filter(form.userId.==)                   // formのUserIdとsessionのUserIdの一致の確認
      .flatMap(UserService.findById)            // Userが存在しているかどうかの確認
      .filter(_.roomId == roomId)               // UserがRoomに所属しているかどうかの確認
      .fold(ifFailed)(ifSuccess)

  def authGuard(form: AuthenticatableForm)(roomId: Long)(implicit request: Request[_]): Boolean = authenticate(form)(roomId)(_ => true)(false)

  case class Form(nickName: String)

  object Form extends FormExtension[Form] {
    val nickName = "nickName"

    val form = data.Form(
      mapping(
        nickName -> text
      )(Form.apply)(Form.unapply)
    )
  }

}
