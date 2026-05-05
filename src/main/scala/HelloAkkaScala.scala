import akka.actor.{Actor, ActorSystem, Inbox, Props}
import scala.concurrent.duration._

// === Actor に送るメッセージを定義する ===
// 挨拶を要求するメッセージ
case object Greet
// 誰に挨拶するかを設定するメッセージ
case class WhoToGreet(who: String)
// Actor から返ってくる挨拶メッセージ
case class Greeting(message: String)

// === 挨拶を管理する Actor ===
class Greeter extends Actor {
  // Actor 内部に状態を持つ
  // Actor はメッセージを1つずつ処理するので、内部状態を扱いやすい
  var greeting = ""
  
  def receive = {
    // WhoToGreet を受け取ったら、挨拶文を更新する
    case WhoToGreet(who) =>
      greeting = s"hello, $who"
    // Greet を受け取ったら、送信元に Greeting を返信する
    case Greet =>
      sender ! Greeting(greeting)
  }
}

// === Greeting を受け取って表示する Actor ===
class GreetPrinter extends Actor {
  def receive = {
    // Greeting メッセージを受け取ったら中身を表示する
    case Greeting(message) =>
      println(message)
  }
}

// === Actor を使う側 ===
object HelloAkkaScala extends App {
  // Actor を動かす土台
  val system = ActorSystem("helloAkka")
  // Greeter Actor を作成し、ActorRef を受け取る
  val greeter = system.actorOf(Props[Greeter](), "greeter")
  // Actor 以外の場所で、Actor からの返信を受け取るための Inbox
  val inbox = Inbox.create(system)

  // === 1回目：akka に挨拶する ===
  // Greeter の状態を "hello, akka" に更新する
  greeter ! WhoToGreet("akka")
  // Inbox から Greeter に Greet を送る
  // Greeter 側では sender が inbox になる
  inbox.send(greeter, Greet)
  // Inbox に届いた Greeting を最大5秒待って受け取る
  val Greeting(message1) = inbox.receive(5.seconds)
  // 受け取った返信を表示する
  println(s"Greeting: $message1")

  // === 2回目：Lightbend に挨拶する ===
  // Greeter の状態を "hello, Lightbend" に更新する
  greeter ! WhoToGreet("Lightbend")
  // Inbox から Greeter に Greet を送る
  inbox.send(greeter, Greet)
  // Inbox に届いた Greeting を受け取る
  val Greeting(message2) = inbox.receive(5.seconds)
  // 受け取った返信を表示する
  println(s"Greeting: $message2")
  
  // === scheduler で定期的にメッセージを送る ===
  // Greeting を受け取って表示する Actor を作る
  val greetPrinter = system.actorOf(Props[GreetPrinter]())

  // 0秒後から、1秒ごとに Greeter に Greet を送る
  // 返信先は greetPrinter
  system.scheduler.schedule(
    0.seconds,
    1.second,
    greeter,
    Greet
  )(system.dispatcher, greetPrinter)
}