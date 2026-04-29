import java.util.concurrent.CyclicBarrier
import java.util.concurrent.BrokenBarrierException

// === CyclicBarrier ===
// 複数スレッドが「全員そろうまで待つ」仕組み
// 指定数に到達すると全スレッドが同時に再開される

// === mainスレッド ===
object BarrierStudy extends App {
  // === CyclicBarrier ===
  // 4スレッドそろったタイミングで Barrier Action 実行
  val barrier = new CyclicBarrier(4, () => {
    println("Barrier Action!")
  })

  // === ワーカスレッド×6 ===
  for (i <- 1 to 6){
    new Thread(() => {
        println(s"Thread started. ${i}")
        Thread.sleep(300)

        // === バリア待機 ===
        // 指定数（4）に達するまでここで待つ（ブロック）
        barrier.await()

        // 4スレッドそろうと一斉に再開される
        Thread.sleep(300)
        println(s"Thread finished. ${i}")
    }).start()
  }

  // メインスレッド終了防止
  Thread.sleep(10000)
  System.exit(0)
}