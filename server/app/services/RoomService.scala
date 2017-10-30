package services

import models.data.{LoginForm, Room}

import scala.collection.mutable.ListBuffer


/**
  * Created by satou on 2017/10/21.
  */
trait RoomRepository {

  /**
    * Roomを登録する
    */
  def entry(room: Room.Form): Option[Room]

  /**
    * URLに対応するRoomを返す
    */
  def findByUrl(url: String): Option[Room]

  /**
    * idに対応するRoomを返す
    */
  def findById(id: Long): Option[Room]

}

trait UsesRoomRepository {
  val roomRepository: RoomRepository
}

/**
  * DBを使ったRoomRepositoryの実装
  */
object RoomRepositoryImpl extends RoomRepository {

  def entry(room: Room.Form): Option[Room] = ???

  def findByUrl(url: String): Option[Room] = ???

  def findById(id: Long): Option[Room] = ???

}

/**
  * DBを使わないRoomRepositoryの実装
  */
class RoomRepositoryMock extends RoomRepository {
  private val repo = ListBuffer.empty[Room]

  def entry(roomForm: Room.Form): Option[Room] = repo.synchronized {
    findByUrl(roomForm.url).fold {
      val room = Room(repo.length, roomForm.url, roomForm.name, roomForm.password)
      repo += room
      Option(room)
    }(_ => None)
  }

  def findByUrl(url: String): Option[Room] = repo.find(_.url == url)

  def findById(id: Long): Option[Room] = repo.find(_.id == id)
}

trait MixInRoomRepository {
  val roomRepository: RoomRepository = new RoomRepositoryMock
}

trait RoomService extends UsesRoomRepository {

  /**
    * Roomをリポジトリに登録する
    * 登録成功時はRoom, 失敗時はNoneを返す
    */
  def entry(roomForm: Room.Form): Option[Room] = roomRepository.entry(roomForm)

  /**
    * URLに対応するRoomを返す
    * 存在する場合はRoom, 存在しない場合はNoneを返す
    */
  def findByUrl(url: String): Option[Room] = roomRepository.findByUrl(url)

  /**
    * idに対応するRoomを返す
    * 存在する場合はRoom, 存在しない場合はNoneを返す
    */
  def findById(id: Long): Option[Room] = roomRepository.findById(id)

  /**
    * URLとパスワードでログインする
    * ログイン成功時はRoom, 失敗時はNoneを返す
    */
  def login(loginForm: LoginForm): Option[Room] =
    roomRepository.findByUrl(loginForm.url).filter(_.password == loginForm.password)

}

object RoomService extends RoomService with MixInRoomRepository
