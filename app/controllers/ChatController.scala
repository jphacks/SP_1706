package controllers

import models.data.{Chat, User}
import play.api.libs.json.Json
import play.api.mvc._
import services.{ChatService, RoomService}
import util.Extension.OptionExtension

/**
  * Created by satou on 2017/10/21.
  */
class ChatController extends Controller {
  implicit private val self = this

  def chatRoom(url: String) = Action { implicit request =>
    User.auth(url) { user =>
      RoomService.findById(user.roomId).map { room =>
        Ok(views.html.chat.room(room))
      }.orInternalServerError
    }
  }

  def ajax = Action { implicit request =>
    Chat.AjaxForm.opt.fold(BadRequest("")) { ajax =>
      Ok(Json.toJson(ChatService.all(ajax.roomId)))
    }
  }

}
