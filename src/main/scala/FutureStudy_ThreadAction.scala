import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

object FutureStudy_ThreadAction extends App {

  // === Future の中身がどのスレッドで動くか確認する ===
  val s = "Hello"
  // Future を作った時点で、別スレッドで処理が開始される
  val f: Future[String] = Future {
    Thread.sleep(1000)
    // Future の中は main スレッドではなく、 ExecutionContext のスレッドプールで実行される
    println(s"[ThreadName] In Future: ${Thread.currentThread.getName}")
    s + " future!"
  }

  // === Future が成功したときの処理 ===
  // Future が成功したら、中身の値を使って処理する
  // この foreach の中も、main スレッドではなくスレッドプール側で動く
  f.foreach { case s: String =>
    println(s"[ThreadName] In Success: ${Thread.currentThread.getName}")
    println(s)
  }

  // === Await で Future の完了を待つ ===
  // まだ 1 秒経っていないので、処理は未完了
  println(f.isCompleted) // false

  // f が完了するまで待つ
  // ただし最大 5000 ミリ秒まで
  Await.ready(f, 5000 millisecond)

  // === App 本体のスレッドを確認する ===
  // App 本体は main スレッドで実行される
  println(s"[ThreadName] In App: ${Thread.currentThread.getName}")
  // Await.ready によって f の完了を待ったので true
  println(f.isCompleted) // true
}