import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging

import scala.concurrent.Await
import scala.concurrent.duration._

// === Actor を定義する ===
// Actor を作るには、Actor トレイトを継承する
class MyActor extends Actor {
  // Akka の Logging を使う
  // ログには、実行スレッド名や Actor のパスも表示される
  val log = Logging(context.system, this)

  // === メッセージを受け取ったときの処理 ===
  // Actor は、メールボックスからメッセージを1つずつ取り出し、 receiveでパターンマッチして処理する
  def receive = {
    // "test" というメッセージを受け取った場合あ
    case "test" => log.info("received test")
    // それ以外のメッセージを受け取った場合
    case _ => log.info("received unknown message")
  }
}

object ActorStudy extends App {
  // === ActorSystem を作る ===
  // Actorを動かすための土台
  // Actor、Actorのパス、メールボックス、設定などすべてを管理
  val system = ActorSystem("actorStudy")

  // === Actor を作る ===
  // Props[MyActor] は、Actor を作るための設定
  // actorOf で Actor を作り、ActorRef を取得する
  val myActor = system.actorOf(Props[MyActor](), "myActor")

  // === Actor にメッセージを送る ===
  // ! は tell メソッドの省略形
  // Actor に非同期でメッセージを送る
  myActor ! "test"
  // "test" 以外なので、unknown message 側にマッチする
  myActor ! "hoge"

  // === アプリを終了させないために main スレッドを止める ===
  // ActorSystem が動き続けるので、このアプリは自動終了しない
  // 停止ボタンで止める
  Thread.currentThread().join()
}