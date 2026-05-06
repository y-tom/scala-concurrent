import akka.actor.{Actor, Props}

// === Supervisor（親Actor 子Actorを管理し、障害時の対応を決める役）===
class Supervisor extends Actor {

  // === Strategy 定義に必要な import（このクラス内だけで使用） ===
  import akka.actor.OneForOneStrategy
  import akka.actor.SupervisorStrategy._
  import scala.concurrent.duration._

  // === 障害時の判断ルール（SupervisorStrategy） ===
  override val supervisorStrategy = {
    // 失敗した子Actorだけに命令する戦略 1分間に最大10回までしか再起動しない
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange  = 1.minute) {
      // 計算エラー → 続行（状態はそのまま）
      case _: ArithmeticException => Resume
      // Null参照 → 再起動（状態リセット）
      case _: NullPointerException => Restart
      // 不正な入力 → 停止
      case _: IllegalArgumentException => Stop
      // その他 → 親に委譲
      case _: Exception => Escalate
    }
  }

  // === 子Actor生成 ===
  def receive = {
    case p: Props =>
      // Props（設計図）から子Actorを生成して返す
      sender() ! context.actorOf(p)
  }
}