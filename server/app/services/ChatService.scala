package services

import java.util.Date

import models.data.Chat
import models.data.Chat.PostForm

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

/**
  * Created by satou on 2017/10/22.
  */
trait ChatRepository {

  /**
    * postFormを投稿し、Chatを返す
    */
  def post(postForm: PostForm): Chat

  /**
    * Roomに投稿されたすべてのChatを返す
    */
  def all(roomId: Long): Seq[Chat]

  /**
    * Roomに投稿されたnoより後のChatを返す
    */
  def versioned(roomId: Long, no: Int): Seq[Chat]
}

object ChatRepositoryMock extends ChatRepository {
  private val repo = ListBuffer.empty[Chat]

  def post(postForm: PostForm): Chat = repo.synchronized {
    val replyTo =
      ChatService.simpleReplyTo(postForm.content)
      .filter(no => versioned(postForm.roomId, no - 1).nonEmpty)
    val chat = Chat(
      id = repo.length,
      roomId = postForm.roomId,
      no = repo.count(_.roomId == postForm.roomId) + 1,
      userId = postForm.userId,
      timeStamp = "%tY-%<tm-%<td %<tH:%<tM:%<tS" format new Date,
      replyTo = replyTo,
      content = postForm.content
    )
    repo += chat
    chat
  }

  def all(roomId: Long): Seq[Chat] = repo.filter(_.roomId == roomId).toList

  def versioned(roomId: Long, no: Int): Seq[Chat] = all(roomId).filter(_.no > no)
}


trait UsesChatRepository {
  val chatRepository: ChatRepository
}

trait MixInChatRepository {
  val chatRepository: ChatRepository = ChatRepositoryMock
}


trait ChatService extends UsesChatRepository {
  private val replyRegex: Regex = "^(@|＠)(\\d+)".r
  def simpleReplyTo(content: String): Option[Int] =
    replyRegex.findFirstMatchIn(content).map(_.group(2).toInt)

  /**
    * PostFormをもとにChatを登録する
    * Roomの存在、Userの存在、Userの所属Roomを調べ、
    * 正しい場合はChatを登録し、そのChatを返す
    * 正しくない場合は何もせずNoneを返す
    */
  def post(postForm: PostForm): Option[Chat] = {
    for {
      room <- RoomService.findById(postForm.roomId)
      user <- UserService.findById(postForm.userId)
      if room.id == user.roomId
    } yield {
      val res = chatRepository.post(postForm)
      println(UserService.findById(res.userId) -> res)
      res
    }
  }

  /**
    * Roomに投稿されたChatを全て返す
    */
  def all(roomId: Long): Seq[Chat] = chatRepository.all(roomId)

  /**
    * Roomに投稿されたChatをno以降のもののみ返す
    */
  def versioned(roomId: Long, no: Int): Seq[Chat] = chatRepository.versioned(roomId, no)
}

object ChatService extends ChatService with MixInChatRepository
