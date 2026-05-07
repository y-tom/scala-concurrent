import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import akka.pattern.ask
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success, Try}

// === メッセージ定義 ===
// 子Actorに「この数を割って」と依頼するメッセージ
case class DivideRandomMessage(numerator: Int)
// 子Actorから返ってくる答え
case class AnswerMessage(num: Int)
// 親Actorに「このリストを分散処理して」と依頼するメッセージ
case class ListDivideRandomMessage(numeratorList: Seq[Int])

// === 子Actor（実際に割り算する役） ===
class RandomDivider extends Actor {
  val random = new Random()
  // 0, 1, 2 のどれかで初期化
  // 0 になったActorは割り算で失敗する
  val denominator = random.nextInt(3)
  def receive = {
    case m @ DivideRandomMessage(numerator) =>
      val answer = Try {
        AnswerMessage(numerator / denominator)
      } match {
        // 割り算に成功した場合
        case Success(a) => a
        // 0除算などで失敗した場合
        case Failure(e) =>
          // 同じメッセージを自分に転送し直す
          // → Restart後の自分に再処理させるため
          self.forward(m)
          // 例外を投げて SupervisorStrategy を発動させる
          throw e
      }
      println(s"$numerator / $denominator is $answer")
      // 親Actorへ答えを返す
      sender() ! answer
  }
}

// === 親Actor（子Actorに処理を分散し、結果を集約する役） ===
class ListRandomDivider extends Actor {
  // 最初に依頼してきた相手を保存しておく
  var listDivideMessageSender = Actor.noSender
  // 子Actorから返ってきた答えの合計
  var sum = 0
  // 返ってきた答えの数
  var answerCount = 0
  // 全部で何個の答えを待つか
  var totalAnswerCount = 0

  // === 障害時の判断ルール ===
  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 10.seconds) {
      // 0除算などの計算エラー → Restart
      case _: ArithmeticException =>
        println("Restart by ArithmeticException")
        Restart
      // 特殊な例外 → Stop
      case _: ActorInitializationException => Stop
      case _: ActorKilledException         => Stop
      case _: DeathPactException           => Stop
      // その他の例外 → Restart
      case _: Exception => Restart
    }

  // === Router 作成 ===
  // 4つの子Actorを作り、順番に処理を振り分ける
  val router = {
    val routees = Vector.fill(4) {
      ActorRefRoutee(context.actorOf(Props[RandomDivider]()))
    }
    Router(RoundRobinRoutingLogic(), routees)
  }
  def receive = {
    // === リスト処理の開始 ===
    case ListDivideRandomMessage(numeratorList) =>
      // 結果を返す相手を保存
      listDivideMessageSender = sender()
      // 待つべき答えの数
      totalAnswerCount = numeratorList.size
      // 各数値を子Actorへ分散して送る
      numeratorList.foreach { n =>
        router.route(DivideRandomMessage(n), self)
      }

    // === 子Actorから答えを受け取る ===
    case AnswerMessage(num) =>
      sum += num
      answerCount += 1
      // 全部の答えが返ってきたら、最初の依頼者へ合計を返す
      if (answerCount == totalAnswerCount) {
        listDivideMessageSender ! sum
      }
  }
}

// === 実行用オブジェクト ===
object RandomDivide extends App {
  implicit val timeout: Timeout = Timeout(10.seconds)
  implicit val system: ActorSystem =
    ActorSystem("randomDivide")
  import system.dispatcher
  // 親Actorを作成
  val listRandomDivider =
    system.actorOf(Props[ListRandomDivider](), "listRandomDivider")
  // 親Actorにリスト処理を依頼し、結果を受け取る
  val result =
    Await.result(
      listRandomDivider ? ListDivideRandomMessage(Seq(1, 2, 3, 4)),
      10.seconds
    )
  println(s"Result: $result")
  Await.ready(system.terminate(), Duration.Inf)
}