import java.util.concurrent.FutureTask

// === FutureTask ===
// 実行可能 + 結果を持てるタスク（Runnable + 結果）
// Thread に渡して実行し、get で結果を待つ

// === mainスレッド ===
object FutureTaskStudy extends App {

  // === タスク定義（まだ実行されていない） ===
  // Callable的な処理（戻り値あり）
  val futureTask = new FutureTask[Int](() => {
    // 重い処理の代わり
    Thread.sleep(1000)
    println("FutureTask finished")
    // この値が get() の戻り値になる
    2525
  })

  // === タスク実行 ===
  // Thread に渡して初めて実行される
  new Thread(futureTask).start()

  // === 別スレッド（結果取得側） ===
  new Thread(() => {
    // 結果が出るまで待つ（ブロック）
    val result = futureTask.get()
    // 完了後に結果を使える
    println(s"result: ${result}")
  }).start()
}