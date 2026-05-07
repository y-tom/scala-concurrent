import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

// === 状態によって振る舞いを切り替えるActor ===
class HotSwapActor extends Actor {
  // become / unbecome を使うため import
  import context._
  // === angry状態 ===
  def angry: Receive = {
    // すでに angry 状態で foo を受け取った
    case "foo" =>
      sender() ! "I am already angry?"
    // bar を受け取ったら happy 状態へ切り替え
    case "bar" =>
      become(happy)
  }

  // === happy状態 ===
  def happy: Receive = {
    // すでに happy 状態で bar を受け取った
    case "bar" =>
      sender() ! "I am already happy :-)"
    // foo を受け取ったら angry 状態へ切り替え
    case "foo" =>
      become(angry)
  }

  // === 初期状態 ===
  // 最初は receive が使われる
  def receive = {
    // foo を受け取ると angry 状態へ
    case "foo" =>
      become(angry)
    // bar を受け取ると happy 状態へ
    case "bar" =>
      become(happy)
  }
}

// === 実行用オブジェクト ===
object HotSwap extends App {
  // ask のタイムアウト設定
  implicit val timeout: Timeout =
    Timeout(5.seconds)
  // ActorSystem 作成
  implicit val system: ActorSystem =
    ActorSystem("hotSwap")
  import system.dispatcher
  
  // === HotSwapActor 作成 ===
  val hotSwapActor =
    system.actorOf(
      Props[HotSwapActor](),
      "hotSwapActor"
    )

  // === angry状態の確認 ===
  // 最初の foo
  // → angry 状態へ切り替わる
  hotSwapActor ! "foo"
  // 2回目の foo
  // → すでに angry なのでメッセージが返る
  val angryResult =
    Await.result(hotSwapActor ? "foo", 5.seconds)
  println("foo: " + angryResult)

  // === happy状態の確認 ===
  // bar を送る
  // → happy 状態へ切り替わる
  hotSwapActor ! "bar"
  // 2回目の bar
  // → すでに happy なのでメッセージが返る
  val happyResult =
    Await.result(hotSwapActor ? "bar", 5.seconds)
  println("bar: " + happyResult)

  // === ActorSystem 終了 ===
  Await.ready(system.terminate(), Duration.Inf)
}