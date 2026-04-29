import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

// === 全体の流れ ===
// main が仕事を詰める（put）
// worker が仕事を取り出して実行する（take → run）
// 全部終わったら worker を止める（interrupt）

// === 構造（Producer-Consumer） ===
// [Producer] mainスレッド
//    ↓ put
// [Queue] BlockingQueue
//    ↓ take
// [Consumer] ワーカスレッド
//    ↓ run
// 処理実行

// === mainスレッド ===
object BlockingQueueStudy extends App {
  // === BlockingQueue ===
  // Runnable を一時的にためる箱 最大10個まで。満杯なら put 側がブロック
  val blockingQueue = new ArrayBlockingQueue[Runnable](10)
  // 終了した仕事数を、複数スレッドから安全に数える
  val finishedCount = new AtomicInteger(0)
  // あとで interrupt するために、起動したワーカを保存する
  var threads = Seq[Thread]()

  // === ワーカスレッド×4（仕事を取り出して実行 = Consumer） ===
  for (i <- 1 to 4) {
    val t = new Thread(() => {
      try {
        while(true){
          // === Runnable(仕事そのもの) 仕事の取得 → 実行 ===
          // キューから仕事を1つ取り出す 空なら仕事が追加されるまでここで待つ（ブロック）
          val runnable = blockingQueue.take()
          // 取り出した仕事をこのスレッド上で実行
          runnable.run()
        }
      } catch {
        // interruptされたら、take/sleepの待機解除->終了
        case _: InterruptedException =>
      }
    })
    // ワーカの起動＆保存
    t.start()
    threads = threads :+ t
  }

  //  === 仕事を作って投入（producer）
  for (i <- 1 to 100) {
    blockingQueue.put(() =>{
      // 重い処理の代わりに1000ms待つ
      Thread.sleep(1000)
      // 仕事完了を表示
      println(s"Runnable: ${i} finished.")
      // 完了数カウント
      finishedCount.incrementAndGet()
    })
  }

  // === 全仕事完了待ち ===
  while (finishedCount.get() != 100) Thread.sleep(1000)
  //  === 停止 ===
  // 全仕事が終わったので、takeで待ち続けているワーカをinterruptして終了させる
  threads.foreach(_.interrupt())
}
