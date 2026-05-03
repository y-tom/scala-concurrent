import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object FutureStudy_ThreadAction extends App {

  // === Future の中身がどのスレッドで動くか確認する ===
  // === mainスレッド ===
  val s = "Hello" // main
  // Future を作った時点で、別スレッドで処理が開始される
  // Futureを作るのはmainスレッド、中身の実行は別スレッド（スレッドプール側）
  val f: Future[String] = Future { // main
    Thread.sleep(1000) // 別
    // Future の中は main スレッドではなく、 ExecutionContext のスレッドプールで実行される
    println(s"[ThreadName] In Future: ${Thread.currentThread.getName}") // 別
    s + " future!" // 別
  }

  // === Future が成功したときの処理 ===
  // Future が成功したら、中身の値を使って処理する
  // foreachの登録はmainスレッド、中身は別スレッド（スレッドプール側）
  f.foreach { case s: String => // main
    println(s"[ThreadName] In Success: ${Thread.currentThread.getName}") // 別
    println(s) // 別
  }

  // === Await で Future の完了を待つ ===
  // まだ 1 秒経っていないので、処理は未完了
  println(f.isCompleted) // false // main

  // f が完了するまで待つ
  // ただし最大 5000 ミリ秒まで
  Await.ready(f, 5000 millisecond) // main

  // === App 本体のスレッドを確認する ===
  // App 本体は main スレッドで実行される
  println(s"[ThreadName] In App: ${Thread.currentThread.getName}") // main
  // Await.ready によって f の完了を待ったので true
  println(f.isCompleted) // true // main
}