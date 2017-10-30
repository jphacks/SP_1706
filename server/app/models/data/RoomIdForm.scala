package models.data

import play.api.data
import play.api.data.Forms._
import play.api.data.format.Formats._
import util.Extension.FormExtension

/**
  * Created by satou on 2017/10/28.
  */
case class RoomIdForm(roomId: Long)

object RoomIdForm extends FormExtension[RoomIdForm] {

  val roomId = "roomId"

  val form = data.Form(
    mapping(
      roomId -> of[Long]
    )(RoomIdForm.apply)(RoomIdForm.unapply)
  )

}
