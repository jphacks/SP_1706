package services

import models.data.Evaluation
import models.data.Evaluation.FromForm

import scala.collection.mutable.ListBuffer
import scala.util.Try

/**
  * Created by satou on 2017/10/25.
  */

trait EvaluationRepository {

  def entry(form: Evaluation.Form): Option[Evaluation]

  def exists(form: Evaluation.Form): Boolean

  def all(roomId: Long): Seq[Evaluation]

}

object EvaluationRepositoryMock extends EvaluationRepository {
  private val repo = ListBuffer.empty[Evaluation]

  def entry(form: Evaluation.Form): Option[Evaluation] = repo.synchronized {
    Evaluation.Type.fromString(form.evaluationType).map { evaluationType =>
      val evaluation = Evaluation(
        id = repo.length,
        roomId = form.roomId,
        no = form.no,
        userId = form.userId,
        evaluationType = evaluationType
      )
      repo += evaluation
      evaluation
    }
  }

  def exists(form: Evaluation.Form): Boolean = repo.exists { eval =>
    eval.roomId == form.roomId &&
      eval.no == form.no &&
      eval.userId == form.userId
  }

  def all(roomId: Long): Seq[Evaluation] = repo.filter(_.roomId == roomId)

}

trait UsesEvaluationRepository {
  val evaluationRepository: EvaluationRepository
}

trait MixInEvaluationRepository {
  val evaluationRepository: EvaluationRepository = EvaluationRepositoryMock
}

trait EvaluationService extends UsesEvaluationRepository {

  /**
    * Evaluation.Formを登録する
    * Chat投稿者と同一人物のEvaluationやRoomに存在しないUserからのEvaluation、2度目のEvaluationは登録せずNoneを返す
    */
  def entry(form: Evaluation.Form): Option[Evaluation] = {
    for {
      _ <- UserService.findById(form.userId)
      chat <- Try(ChatService.all(form.roomId)(form.no - 1)).toOption
      if chat.userId != form.userId
      if !evaluationRepository.exists(form)
      result <- evaluationRepository.entry(form)
    } yield result
  }

  /**
    * Room内のすべてのEvaluationを取得する
    */
  def all(roomId: Long): Seq[Evaluation] = evaluationRepository.all(roomId)

  /**
    * Room内のすべてのChatに対し、いくつEvaluationがあるかをカウントし、
    * Map[no, count]の形式で返す
    */
  def resultMap(roomId: Long): Map[Int, Int] = ChatService.all(roomId).map { chat =>
    chat.no -> all(roomId).count(_.no == chat.no)
  }.toMap

  /**
    * Room内の自身の発言に対するEvaluationの送信元Userのニックネームを
    * 発言ごとにまとめて返す
    */
  def evaluationFrom(fromForm: FromForm): Map[Int, Seq[String]] = {
    ChatService
      .all(fromForm.roomId)
      .filter(_.userId == fromForm.userId)
      .map { chat =>
        chat.no ->
          all(fromForm.roomId)
            .filter(_.no == chat.no)
            .flatMap(eval => UserService.findById(eval.userId).map(_.nickName))
      }
      .groupBy(_._1)
      .mapValues(_.flatMap(_._2))
  }

  /**
    * Room内のChatを、Evaluationの昇順・noの降順に並び替え、その順にnoを返す
    */
  def ranking(roomId: Long): Seq[Int] =
    all(roomId)
      .groupBy(_.no)
      .mapValues(_.size)
      .toSeq
      .sortWith {
        case ((n1, e1), (n2, e2)) => e1 > e2 || n1 < n2
      }
      .map(_._1)
}

object EvaluationService extends EvaluationService with MixInEvaluationRepository
