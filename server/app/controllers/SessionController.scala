package controllers

import models.data.{LoginForm, User}
import play.api.mvc._
import services.{RoomService, UserService}
import util.Extension.OptionExtension

/**
  * Created by satou on 2017/10/22.
  */
class SessionController extends Controller {
  implicit private val self = this

  def login = Action { implicit request =>
    (for {
      loginForm <- LoginForm.opt
      userForm <- User.Form.opt
      room <- RoomService.login(loginForm)
      user <- UserService.entry(room.url, userForm)
    } yield {
      Redirect(routes.ChatController.chatRoom(room.url)).withSession(
        request.session + (User.sessionName(room.id), user.id.toString)
      )
    }).fold {
      Ok("失敗")
    }(identity)
  }

}
