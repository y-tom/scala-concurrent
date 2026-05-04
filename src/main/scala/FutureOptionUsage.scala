import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

object FutureOptionUsage extends App {

  // === ランダムな待ち時間を用意する ===
  val random = new Random() // main
  val waitMaxMilliSec = 3000 // main

  // === Future で非同期処理を作る ===
  // Future を作った時点で、別スレッドで処理が開始される
  // 成功すれば「待ったミリ秒」が Int として返る
  // 失敗すれば RuntimeException を持った Future になる
  // mainスレッドで Future を作る、中身は別スレッド
  val futureMillSec: Future[Int] = Future { // main
    val waitMilliSec = random.nextInt(waitMaxMilliSec)
    // 待ち時間が1000ミリ秒未満なら、わざと失敗させる
    // Future の中で例外が起きると、その Future は失敗状態になる
    if (waitMilliSec < 1000)
      throw new RuntimeException(s"waitMilliSec is ${waitMilliSec}")
    // 1000 ミリ秒以上なら、その時間だけ待つ
    Thread.sleep(waitMilliSec)
    // Future の成功結果になる値 この Future は Future[Int] になる
    waitMilliSec
  }

  // === Futureの中身をmapで変換する ===
  // Future[Int] の中身をミリ秒 → 秒に変換
  // Option と同じように、Future も map で中身を変換できる
  //　mainスレッドで「map処理(futureMillSec が成功した後の変換処理)」を登録する、成功後に実行される中身は別スレッド
  val futureSec: Future[Double] = futureMillSec.map(i => // main
    i.toDouble / 1000)

  // === Future の成功・失敗を処理する ===
  // onComplete は、Future が完了したあとに実行される
  // 成功なら Success、失敗なら Failure に入る
  // mainスレッドで「完了後の処理」を登録する、中身は別スレッド
  futureSec onComplete { // main
    case Success(waitSec) =>
      println(s"Success! ${waitSec} sec")
    case Failure(t) =>
      println(s"Failure: ${t.getMessage}")
  }

  // === App がすぐ終了しないように待つ ===
  // mainスレッドを止める
  // Futureの処理が終わる前にAppが終了しないようにしている
  Thread.sleep(5000) // main
}