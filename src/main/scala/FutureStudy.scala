import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object FutureStudy extends App {

  // === 成功する Future を作る ===
  val s = "Hello"
  // Future を作った時点で、別スレッドで処理が開始される
  // 1000 ミリ秒待機して、"Hello" と " future!" を文字列結合する
  val f: Future[String] = Future {
    Thread.sleep(1000)
    s + " future!"
  }

  // === 成功したときの処理 ===
  // Future が成功したとき、中身の値を使って処理する
  // JavaScript の Promise.then に近いイメージ
  f.foreach { case s: String =>
    println(s)
  }

  // === 成功 Future の完了状態を確認 ===
  // まだ 1 秒経っていないので、処理は未完了
  println(f.isCompleted) // false
  // メインスレッドを待たせて、Future の完了を待つ
  Thread.sleep(5000) // Hello future!
  // Future の処理が終わったので true
  // 未完了 → 完了に変わることを確認する
  println(f.isCompleted) // true


  // === 失敗する Future を作る ===
  // Future の中で例外を投げる
  val f2: Future[String] = Future {
    Thread.sleep(1000)
    throw new RuntimeException("わざと失敗")
  }

  // === 失敗したときの処理 ===
  // Future が失敗したとき、例外を取り出して処理する
  f2.failed.foreach { case e: Throwable =>
    println(e.getMessage)
  }

  // === 失敗 Future の完了状態を確認 ===

  // 例外が起きる前なので未完了
  println(f2.isCompleted) // false

  // Future の失敗処理が終わるまで待つ
  Thread.sleep(5000) // わざと失敗

  // 成功でも失敗でも、処理が終われば completed になる
  println(f2.isCompleted) // true

}