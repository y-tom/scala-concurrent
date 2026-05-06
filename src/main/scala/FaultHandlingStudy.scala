import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

object FaultHandlingStudy extends App {

  // === ask のタイムアウト設定 ===
  implicit val timeout: Timeout = Timeout(5.seconds)
  // === ActorSystem 作成 ===
  implicit val system: ActorSystem =
    ActorSystem("faultHandlingStudy")
  // ask 用 ExecutionContext
  import system.dispatcher

  // === Supervisor 作成 ===
  val supervisor =
    system.actorOf(Props[Supervisor](), "supervisor")

  // === Child 作成 ===
  // Supervisor に Child の Props を送り、 子Actorを生成して返してもらう
  val child =
    Await.result(
      (supervisor ? Props[Child]()).mapTo[ActorRef],
      5.seconds
    )

  // === 通常動作 ===
  // state = 42 に更新
  child ! 42
  // "get" を ask で送り、現在の state を取得
  println(
    "set state to 42: " +
      Await.result(child ? "get", 5.seconds)
  ) // 42 expected

  // === Resume の確認 ===
  // ArithmeticException → Resume
  // Actorは続行されるので state は保持される
  child ! new ArithmeticException
  println(
    "crash it: " +
      Await.result(child ? "get", 5.seconds)
  ) // 42 expected

  // === Restart の確認 ===
  // NullPointerException → Restart
  // Actor が作り直されるので state は 0 に戻る
  child ! new NullPointerException
  println(
    "crash it harder: " +
      Await.result(child ? "get", 5.seconds)
  ) // 0 expected

  // === Stop の確認 ===
  // IllegalArgumentException → Stop
  // Actor が停止される
  child ! new IllegalArgumentException

  // === 新しい Child を作成 ===
  val child2 =
    Await.result(
      (supervisor ? Props[Child]()).mapTo[ActorRef],
      5.seconds
    )

  println(
    "new child: " +
      Await.result(child2 ? "get", 5.seconds)
  ) // 0 expected

  // === Escalate の確認 ===
  // Exception → Escalate
  // Supervisor の親へ処理を委譲
  child2 ! new Exception("CRASH")

  // === ActorSystem 終了 ===
  Await.ready(system.terminate(), Duration.Inf)
}