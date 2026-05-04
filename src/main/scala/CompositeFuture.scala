import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.util.{Failure, Success, Random}

object CompositeFuture extends App {

  // === ランダムな待ち時間を用意する ===
  val random = new Random() // main
  val waitMaxMilliSec = 3000 // main

  // === Future で実行する共通処理 ===
  // ランダムな時間だけ待つ処理
  // 500ミリ秒未満なら、わざと失敗させる
  def waitRandom(futureName: String): Int = { // mainでメソッド定義、中身の実行はスレッドプール側
    val waitMilliSec = random.nextInt(waitMaxMilliSec)
    // 待ち時間が短すぎる場合は失敗扱いにする
    if (waitMilliSec < 500)
      throw new RuntimeException(s"${futureName} waitMilliSec is ${waitMilliSec}")
    // ランダムに決まった時間だけ待つ
    Thread.sleep(waitMilliSec)
    // 成功した場合、この値が Future の結果になる
    waitMilliSec
  }

  // === 2つの Future を作る ===
  // Future を作った時点で、別スレッドで処理が開始される
  // 成功すれば first の待ち時間が Int で返る
  val futureFirst: Future[Int] = Future { // mainで作成
    waitRandom("first") // 実行はスレッドプール側
  }
  // こちらも作った時点で、別スレッドで処理が開始される
  // futureFirst とは別の Future として動く
  val futureSecond: Future[Int] = Future { // mainで作成
    waitRandom("second") // 実行はスレッドプール側
  }

  // === for式で Future を組み合わせる ===
  // 2つの Future を組み合わせて、新しい Future を作る
  // 両方成功したときだけ、(first, second) のタプルになる
  val compositeFuture: Future[(Int, Int)] = for { // mainで組み合わせ処理を登録
    first <- futureFirst // 成功したあと、中身を取り出してyieldする処理はスレッドプール側で動く
    second <- futureSecond
  } yield (first, second)

  // === 組み合わせた Future の結果を処理する ===
  // compositeFuture が完了したあとに実行される
  compositeFuture onComplete { // mainで完了後の処理を登録、中身の実行はスレッドプール側
    // 両方の Future が成功した場合
    case Success((first, second)) =>
      println(s"Success! first:${first} second:${second}")
    // どちらかの Future が失敗した場合
    case Failure(t) =>
      println(s"Failure: ${t.getMessage}")
  }

  // === App がすぐ終了しないように待つ ===
  // Future は別スレッドで動くため、 mainスレッドが先に終わらないように待つ
  Thread.sleep(5000) // main
}