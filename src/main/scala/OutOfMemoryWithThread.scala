import java.util.concurrent.atomic.AtomicInteger

// === 危険な実装（Threadを無限生成） ===
// Threadを作り続けると、
// === mainスレッド ===
object OutOfMemoryWithThread extends App {
  // 作成されたスレッド数をカウント
  val counter = new AtomicInteger(0)

  // 無限にスレッドを生成し続ける
  // while は mainスレッドで実行されている（スレッド生成ループ）
  while (true) {
    // === 別スレッド ===
    new Thread(() => {
      // 何個目のスレッドか表示
      println(counter.incrementAndGet())
      // 長時間生き続ける（= スレッドが溜まり続ける）
      Thread.sleep(100000)
    }).start() // スレッド生成 + 即実行
  }
}