package services

import models.data.Chat.PostForm
import models.data.Evaluation.Good
import models.data.{Evaluation, LoginForm, Room, User}
import org.scalatestplus.play.PlaySpec

/**
  * Created by satou on 2017/10/22.
  */
class ServiceSpec extends PlaySpec {

  private val room1Form = Room.Form("/test/url", "テストルーム", "password")
  private val room2Form = room1Form.copy(url = "/test/url/xxx")

  private val room1 = Room(0, "/test/url", "テストルーム", "password")
  private val room2 = room1.copy(id = 1, url = "/test/url/xxx")

  "RoomService" should {

    "Roomのエントリー" in {
      RoomService.entry(room1Form) mustBe Some(room1)
      RoomService.entry(room2Form) mustBe Some(room2)
      RoomService.entry(room1Form) mustBe None
    }

    "RoomのURL検索" in {
      RoomService.findByUrl("/test/url") mustBe Some(room1)
      RoomService.findByUrl("/test/url/xxx") mustBe Some(room2)
      RoomService.findByUrl("/test/url/xxx/yyy") mustBe None
    }

    "Roomのid検索" in {
      RoomService.findById(-1) mustBe None
      RoomService.findById(0) mustBe Some(room1)
      RoomService.findById(1) mustBe Some(room2)
      RoomService.findById(2) mustBe None
    }

    "Roomにログイン" in {
      RoomService.login(LoginForm("/test/url", "password")) mustBe Some(room1)
      RoomService.login(LoginForm("/test/url/xxx", "password")) mustBe Some(room2)
      RoomService.login(LoginForm("/test/url/yyy", "password")) mustBe None
      RoomService.login(LoginForm("/test/url", "dummy")) mustBe None
    }

  }

  "UserService" should {

    "Userのエントリー" in {
      UserService.entry("/test/url", User.Form("XXX")) mustBe Some(User(0, room1.id, "XXX"))
      UserService.entry("/test/url", User.Form("XXX")) mustBe None
      UserService.entry("/test/url/xxx", User.Form("XXX")) mustBe Some(User(1, room2.id, "XXX"))
      UserService.entry("/test/url/xxx", User.Form("YYY")) mustBe Some(User(2, room2.id, "YYY"))
      UserService.entry("/test/url/yyy", User.Form("ZZZ")) mustBe None
      UserService.entry("/test/url", User.Form("EvaluationUser")) mustBe Some(User(3, room1.id, "EvaluationUser"))
      UserService.entry("/test/url/xxx", User.Form("EvaluationUser")) mustBe Some(User(4, room2.id, "EvaluationUser"))
    }

    "Userのid検索" in {
      UserService.findById(-1) mustBe None
      UserService.findById(0) mustBe Some(User(0, room1.id, "XXX"))
      UserService.findById(1) mustBe Some(User(1, room2.id, "XXX"))
      UserService.findById(2) mustBe Some(User(2, room2.id, "YYY"))
      UserService.findById(3) mustBe Some(User(3, room1.id, "EvaluationUser"))
      UserService.findById(4) mustBe Some(User(4, room2.id, "EvaluationUser"))
    }

  }

  "ChatService" should {

    "Chatのエントリー" in {
      ChatService.post(PostForm(room1.id, 0, None, "chat1"))
      ChatService.post(PostForm(room1.id, 0, None, "chat2"))
      ChatService.post(PostForm(room1.id, 1, None, "chat3")) mustBe None
      ChatService.post(PostForm(room2.id, 0, None, "chat4")) mustBe None
      ChatService.post(PostForm(room2.id, 1, None, "chat5"))
    }

    "Chatの取得" in {
      ChatService.all(room1.id).length mustBe 2
      ChatService.all(room2.id).length mustBe 1
    }

    "増分Chatの取得" in {
      ChatService.versioned(room1.id, 0).length mustBe 2
      ChatService.versioned(room1.id, 1).length mustBe 1
      ChatService.versioned(room1.id, 2).length mustBe 0
      ChatService.versioned(room2.id, 0).length mustBe 1
    }

  }

  "EvaluationService" should {

    "Evaluationのエントリー" in {
      // room1 --> id 0
      //    1: userId 0
      //    2: userId 0
      // room2 --> id 1
      //    1: userId 1
      // Evaluation.Form(roomId, no, userId, evaluationType)
      EvaluationService.entry(Evaluation.Form(0, 1, 3, "Good")) mustBe Some(Evaluation(0, 0, 1, 3, Good))
      EvaluationService.entry(Evaluation.Form(0, 2, 3, "Good")) mustBe Some(Evaluation(1, 0, 2, 3, Good))
      EvaluationService.entry(Evaluation.Form(0, 3, 3, "Good")) mustBe None     // 存在しないnoには評価できない
      EvaluationService.entry(Evaluation.Form(1, 1, 4, "Good")) mustBe Some(Evaluation(2, 1, 1, 4, Good))
      EvaluationService.entry(Evaluation.Form(1, 1, 4, "Good")) mustBe None     // 全く同じ評価は2度できない
      EvaluationService.entry(Evaluation.Form(0, 1, 0, "Good")) mustBe None     // 投稿者は評価できない
      EvaluationService.entry(Evaluation.Form(0, 1, 3, "Gooood")) mustBe None
    }

    "Evaluationの取得" in {
      EvaluationService.all(0).length mustBe 2
      EvaluationService.all(1).length mustBe 1
      EvaluationService.all(2).length mustBe 0
    }

    "評価元ニックネームの取得" in {
      EvaluationService.evaluationFrom(0, 0) mustBe Map(1 -> Seq("EvaluationUser"), 2 -> Seq("EvaluationUser"))
      EvaluationService.evaluationFrom(0, 1) mustBe Map.empty
      EvaluationService.evaluationFrom(1, 1) mustBe Map(1 -> Seq("EvaluationUser"))
    }

  }

}
