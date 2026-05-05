import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

// === Actor に送るメッセージを定義する ===
case object Greet
case class WhoToGreet(who: String)
case class Greeting(message: String)

// === 挨拶を管理する Actor ===
class Greeter extends Actor {
  var greeting = ""

  def receive = {
    // WhoToGreet を受け取ったら、挨拶文を更新する
    case WhoToGreet(who) =>
      greeting = s"hello, $who"
    // Greet を受け取ったら、送信元に Greeting を返信する
    case Greet =>
      sender() ! Greeting(greeting)
  }
}


// === Greeting を受け取って表示する Actor ===
class GreetPrinter extends Actor {
  def receive = {
    case Greeting(message) =>
      println(message)
  }
}

// === Actor を使う側 ===
object HelloAkkaScala extends App {
  given Timeout = Timeout(5.seconds)
  val system = ActorSystem("helloAkka")
  val greeter = system.actorOf(Props[Greeter](), "greeter")

  // === 1回目：akka に挨拶する ===
  greeter ! WhoToGreet("akka")
  val greeting1 =
    Await.result((greeter ? Greet).mapTo[Greeting], 5.seconds)
  println(s"Greeting: ${greeting1.message}")

  // === 2回目：Lightbend に挨拶する ===
  greeter ! WhoToGreet("Lightbend")
  val greeting2 =
    Await.result((greeter ? Greet).mapTo[Greeting], 5.seconds)
  println(s"Greeting: ${greeting2.message}")

  // === scheduler で定期的にメッセージを送る ===
  val greetPrinter = system.actorOf(Props[GreetPrinter]())
  system.scheduler.scheduleWithFixedDelay(
    0.seconds,
    1.second,
    greeter,
    Greet
  )(system.dispatcher, greetPrinter)
}