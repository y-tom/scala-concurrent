import java.util.concurrent.Semaphore

// === Semaphore ===
// 同時に実行できるスレッド数を制限する仕組み
// acquire で許可を取得し、release で返却する

// === mainスレッド ===
object SemaphoreStudy extends App{
  // === semaphore ===
  // 同時に3スレッドまで実行可能
  val semaphore = new Semaphore(3)

  for (i <- 1 to 100) {
    new Thread(() => {
      try{
        // === 許可取得 ===
        // 空きがなければここで待つ（ブロック）
        semaphore.acquire()
        // 処理（同時に最大3つだけ動く）
        Thread.sleep(300)
        println(s"Thread finished. ${i}")
      }finally{
        // === 許可返却 ===
        // 次の待機スレッドが進めるようになる
        semaphore.release()
      }
    }).start()
  }
}
