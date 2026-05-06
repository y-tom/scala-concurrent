import akka.actor.Actor

// === 子Actor（状態を持ち、例外を発生させる役） ===
class Child extends Actor {

  // === 内部状態（Actorごとに保持される値） ===
  var state = 0

  def receive = {
    // === 例外を発生させる ===
    // 外から Exception を送ると、そのまま投げる
    // → Supervisor に検知させて、Strategy を発動させるため
    case ex: Exception => throw ex
    // === 状態更新 ===
    // Int を受け取ると、内部状態を書き換える
    case x: Int => state = x
    // === 状態取得 ===
    // "get" を受け取ると、現在の状態を送り返す
    // → Restart で状態がどう変わるか確認できる
    case "get" => sender() ! state
  }
}