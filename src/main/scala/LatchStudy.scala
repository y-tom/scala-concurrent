import java.util.concurrent.CountDownLatch

// === CountDownLatch ===
// 複数スレッドの完了を待つための同期機構
// countDown でカウントを減らし、0になると await が解除される

// === mainスレッド ===
object LatchStudy extends App{
  // 3つの仕事の完了を待つ
  val latch = new CountDownLatch(3)

  // === ワーカスレッド×3（countDownする側） ===
  for (i <- 1 to 3){
    new Thread (() => {
      println(s"Finished and countDown! ${i}")
      latch.countDown()
    }).start()
  }

  // === ワーカスレッドLast（待つ側） ===
  new Thread(() => {
    // カウントが0になるまで待機（ブロック）
    latch.await()
    println("All tasks finished.")
  }).start()
}
