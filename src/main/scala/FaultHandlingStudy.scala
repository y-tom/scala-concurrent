import akka.actor.{ActorRef, ActorSystem, Inbox, Props}

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object FaultHandlingStudy extends App {

  // === ActorSystem / Inbox 作成 ===
  val system = ActorSystem("faultHandlingStudy")
  val inbox = Inbox.create(system)
  // ! で送るときの sender を inbox にする
  implicit val sender = inbox.getRef()

  // === Supervisor 作成 ===
  val supervisor = system.actorOf(Props[Supervisor], "supervisor")

  // === Child 作成 ===
  // Supervisor に Props[Child] を送り、子Actorを作ってもらう
  supervisor ! Props[Child]
  val child = inbox.receive(5.seconds).asInstanceOf[ActorRef]

  // === 通常動作 ===
  child ! 42
  child ! "get"
  println("set state to 42: " + inbox.receive(5.seconds)) // 42

  // === Resume の確認 ===
  // ArithmeticException → Resume
  // 状態はそのままなので 42
  child ! new ArithmeticException
  child ! "get"
  println("crash it: " + inbox.receive(5.seconds)) // 42

  // === Restart の確認 ===
  // NullPointerException → Restart
  // Actor が作り直されるので state は 0
  child ! new NullPointerException
  child ! "get"
  println("crash it harder: " + inbox.receive(5.seconds)) // 0

  // === Stop の確認 ===
  // IllegalArgumentException → Stop
  // watch しているので Terminated を受け取る
  inbox.watch(child)
  child ! new IllegalArgumentException
  println("watch and break it: " + inbox.receive(5.seconds))

  // === 新しい Child を作成 ===
  supervisor ! Props[Child]
  val child2 = inbox.receive(5.seconds).asInstanceOf[ActorRef]

  inbox.watch(child2)
  child2 ! "get"
  println("new child: " + inbox.receive(5.seconds)) // 0

  // === Escalate の確認 ===
  // Exception → Escalate
  // 親Actorへ処理を委譲する
  child2 ! new Exception("CRASH")
  println("escalate failure: " + inbox.receive(5.seconds))

  Await.ready(system.terminate(), Duration.Inf)
}