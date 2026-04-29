import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Callable, Executors}

// === mainスレッド ===
object ExecutorServiceStudy extends App {

  // === スレッドプール作成 ===
  // 10スレッドでタスクを処理する
  val es = Executors.newFixedThreadPool(10)

  // 複数スレッドから安全にカウント
  val counter = new AtomicInteger(0)

  // === タスク投入 ===
  // 1000個のタスクをスレッドプールに投げる
  val futures = for (i <- 1 to 1000) yield {
    // submitするとFutureが返る（結果を後で取得できる）
    es.submit(new Callable[Int] {
      // === タスクの中身（別スレッドで実行） ===
      override def call(): Int = {
        val count = counter.incrementAndGet()
        println(count)
        // 重い処理の代わり
        Thread.sleep(100)
        // この値がFutureの結果になる
        count
      }
    })
  }

  // === 結果取得 ===
  // Future.get()で結果を取り出す（ここでブロック）
  println("sum: " + futures.foldLeft(0)((acc, f) => acc + f.get()))

  // === シャットダウン ===
  // スレッドプールを停止（必須）
  es.shutdownNow()
}