package models.data

import play.api.data
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.json.Json
import util.Extension.{AuthenticatableForm, FormExtension}

/**
  * Created by satou on 2017/10/21.
  */
case class Evaluation(id: Long, roomId: Long, no: Int, userId: Long, evaluationType: Evaluation.Type) extends AuthenticatableForm {
  def toAjaxResult: Evaluation.AjaxResult = Evaluation.AjaxResult(no, evaluationType.toString)
}

object Evaluation {

  case class Form(roomId: Long, no: Int, userId: Long, evaluationType: String) extends AuthenticatableForm

  object Form extends FormExtension[Form] {

    val roomId = "roomId"
    val no = "no"
    val userId = "userId"
    val evaluationType = "evaluationType"

    val form = data.Form(
      mapping(
        roomId -> of[Long],
        no -> number,
        userId -> of[Long],
        evaluationType -> text
      )(Form.apply)(Form.unapply)
    )
  }


  case class FromForm(roomId: Long, userId: Long) extends AuthenticatableForm

  object FromForm extends FormExtension[FromForm] {
    val roomId = "roomId"
    val userId = "userId"
    val form = data.Form(
      mapping(
        roomId -> of[Long],
        userId -> of[Long]
      )(FromForm.apply)(FromForm.unapply)
    )
  }

  case class AjaxResult(no: Int, evaluation: String)

  object AjaxResult {
    implicit def jsonWrites = Json.writes[AjaxResult]
  }

  sealed trait Type

  object Type {

    def all: Seq[Type] = Seq(Good)

    def fromString(seed: String): Option[Type] = all.find(_.toString == seed)

  }

  case object Good extends Type
}

