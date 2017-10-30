package controllers

import models.data.Chat.{AjaxForm, PostForm}
import models.data.{Chat, Evaluation, RoomIdForm, User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.json.Json
import play.api.mvc._
import services._
import util.Extension.OptionExtension
import util.Extension

/**
  * Created by satou on 2017/10/21.
  */
class ChatController extends Controller with Extension.MapJsonWrite {
  implicit private val self = this

  def chatRoom(url: String) = Action { implicit request =>
    RoomService.findByUrl(url).map { room =>
      User.authenticate(room.id){ user =>
        Ok(views.html.chat.room(room, user))
      }(Ok(views.html.login(room.url)))
    }.getOrElse(BadRequest)
  }

  def postApi = Action { implicit request =>
    (for {
      postForm <- PostForm.opt
      if User.authGuard(postForm)(postForm.roomId)
      room <- RoomService.findById(postForm.roomId)
      user <- UserService.findById(postForm.userId)
      if user.roomId == room.id
      chat <- ChatService.post(postForm)
    } yield {
      Ok(Json.toJson(chat))
    }).orBadRequest
  }

  def getApi = Action { implicit request =>
    Chat.AjaxForm.opt.map { ajaxForm =>
      User.authenticate(ajaxForm)(ajaxForm.roomId) { _ =>
        Ok(Json.toJson(ChatService.versioned(ajaxForm.roomId, ajaxForm.no).map(_.toResult(Some(ajaxForm.userId)))))
      }(Forbidden)
    }.orBadRequest
  }

  def evaluationApi = Action { implicit request =>
    (for {
      evaluationForm <- Evaluation.Form.opt
      if User.authGuard(evaluationForm)(evaluationForm.roomId)
      room <- RoomService.findById(evaluationForm.roomId)
      user <- UserService.findById(evaluationForm.userId)
      if room.id == user.roomId
      _ <- EvaluationService.entry(evaluationForm)
    } yield {
      Ok
    }).orBadRequest
  }

  def getEvaluationApi = Action { implicit request =>
    RoomIdForm.opt.map { form =>
      User.authenticate(form.roomId) { _ =>
        Ok(Json.toJson(EvaluationService.resultMap(form.roomId)))
      }(Forbidden)
    }.orBadRequest
  }

  def getEvaluationFromApi = Action { implicit request =>
    Evaluation.FromForm.opt.map { form =>
      User.authenticate(form)(form.roomId) { _ =>
        Ok(Json.toJson(EvaluationService.evaluationFrom(form)))
      }(Forbidden)
    }.orBadRequest
  }

  def rankingApi = Action { implicit request =>
    RoomIdForm.opt.map { form =>
      User.authenticate(form.roomId) { _ =>
        val ranking = EvaluationService.ranking(form.roomId)
        val all = ChatService.all(form.roomId).map(_.toResult())
        Ok(Json.toJson(ranking.flatMap(no => all.find(_.no == no))))
      }(Forbidden)
    }.orBadRequest
  }

}
