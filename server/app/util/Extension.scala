package util

import play.api.data.Form
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{Controller, Request, Result}

/**
  * Created by satou on 2017/10/21.
  */
object Extension {
  /**
    * Formの拡張機能
    */
  trait FormExtension[A] {
    val form: Form[A]

    /**
      * requestからFormを受け取れた場合はSome(Form)、受け取れなかった場合はNoneを返す
      */
    def opt(implicit request: Request[_]): Option[A] = form.bindFromRequest().fold(_ => None, Some.apply)
  }

  /**
    * 認証Formのインタフェース
    */
  trait AuthenticatableForm {
    val userId: Long
  }

  /**
    * Option[Result]の拡張機能
    */
  implicit class OptionExtension(opt: Option[Result]) {
    /**
      * Option値に中身があればその値、なければBadRequestを返す
      */
    def orBadRequest(implicit controller: Controller): Result =
      opt.getOrElse(controller.BadRequest("parameter problem."))

    /**
      * Option値に中身があればその値、なければInternalServerErrorを返す
      */
    def orInternalServerError(implicit controller: Controller): Result =
      opt.getOrElse(controller.InternalServerError("sorry, server problem occurred."))
  }

  /**
    * JsonWriteの暗黙の値を提供する
    */
  trait MapJsonWrite {
    /**
      * Map[Int, Int]をjsonに変換するための値
      */
    protected implicit val mapInt2IntWrites: Writes[Map[Int, Int]] = new Writes[Map[Int, Int]] {
      def writes(map: Map[Int, Int]): JsValue =
        Json.obj(map.map { case (s, o) =>
          val ret: (String, JsValueWrapper) = s.toString -> o
          ret
        }.toSeq: _*)
    }

    /**
      * Map[Int, Int]をjsonに変換するための値
      */
    protected implicit val mapInt2SeqStringWrites: Writes[Map[Int, Seq[String]]] = new Writes[Map[Int, Seq[String]]] {
      def writes(map: Map[Int, Seq[String]]): JsValue =
        Json.obj(map.map {
          case (key, value) =>
            val ret: (String, JsValueWrapper) = key.toString -> value
            ret
        }.toSeq: _*)
    }
  }


}
