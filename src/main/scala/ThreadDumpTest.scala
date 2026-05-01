import java.time.LocalDateTime
import java.util.concurrent.{Executors, TimeUnit}

// === mainスレッド ===
object ThreadDumpTest extends App {

  // === スレッドプール作成 ===
  // 3スレッドで定期タスクを実行する
  val es = Executors.newScheduledThreadPool(3)
gi
  // === 定期タスク登録 ===
  // 1秒ごとにタスクを実行し続ける（終了しない）
  // scheduleAtFixedRate(タスク, 初回遅延, 間隔, 単位)
  es.scheduleAtFixedRate(() => {


    // === 実行中スレッドの情報を出力 ===
    // どのスレッドが動いているか確認できる
    println(
      s"ThreadName:${Thread.currentThread().getName} " +
        s"LocalDateTime:${LocalDateTime.now()}"
    )
  },
    0L,                // 初回遅延（0秒ですぐ開始）
    1L,                // 実行間隔（1秒ごと）
    TimeUnit.SECONDS   // 時間単位
  )

  // ※ shutdownしないため、スレッドは待機し続ける
  // → ThreadDump / VisualVM で状態観測するためのコード
}