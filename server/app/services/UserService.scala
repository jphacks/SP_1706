package services

import models.data.User

import scala.collection.mutable.ListBuffer

/**
  * Created by satou on 2017/10/21.
  */
trait UserRepository {
  /**
    * UserをurlのRoomに新規登録する
    */
  def entry(url: String, userForm: User.Form): Option[User]

  /**
    * idに対応するUserを返す
    */
  def findById(id: Long): Option[User]
}

object UserRepositoryMock extends UserRepository {
  private val repo = ListBuffer.empty[User]

  def entry(url: String, userForm: User.Form): Option[User] = repo.synchronized {
    repo
      .find(user => user.nickName == userForm.nickName && RoomService.findById(user.roomId).forall(_.url == url))
      .fold {
        RoomService.findByUrl(url).map { room =>
          val user = User(repo.length, room.id, userForm.nickName)
          repo += user
          user
        }
      }(_ => None)
  }

  def findById(id: Long): Option[User] = repo.find(_.id == id)
}

object UserRepositoryImpl extends UserRepository {
  def entry(url: String, userForm: User.Form): Option[User] = ???

  def findById(id: Long): Option[User] = ???
}

trait UsesUserRepository {
  val userRepository: UserRepository
}

trait MixInUserRepository {
  val userRepository = UserRepositoryMock
}

trait UserService extends UsesUserRepository {
  /**
    * Userをurlのroomに登録する
    * 登録成功時は登録したUser、失敗時はNoneを返す
    */
  def entry(url: String, userForm: User.Form): Option[User] = userRepository.entry(url, userForm)

  /**
    * idに対応するUserを返す
    * 存在する場合はUser, 存在しない場合はNoneを返す
    */
  def findById(id: Long): Option[User] = userRepository.findById(id)
}

object UserService extends UserService with MixInUserRepository