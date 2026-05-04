import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Promise, Future}
import scala.concurrent.duration._

object PromiseStudy extends App {

  // === Promise から Future を作る ===
  // Promise は「結果を入れる側」。success/failureを「あとから」設定できる
  val promiseGetInt: Promise[Int] = Promise[Int]  // main
  // Future は「結果を受け取る側」。まだ成功も失敗もしていない未完了状態
  // Promiseから作られ、Promiseが完了するとこのFutureも完了する
  val futureByPromise: Future[Int] = promiseGetInt.future // main

  // === Promise から作った Future に処理を登録する ===
  // mainスレッドで map 処理を登録する
  // futureByPromise が成功したあと、中身はスレッドプール側で実行される
  val mappedFuture = futureByPromise.map { i =>
    // Promise に success(1) が設定されたあとに実行される
    println(s"Success! i: ${i}")
  }

  // mainスレッドで Future を作る、中身の実行はスレッドプール側
  Future {
    // 300ミリ秒待つ
    Thread.sleep(300)
    // Promise に成功値 1 を入れる
    // これにより、promiseGetInt.future が成功状態になる
    promiseGetInt.success(1)
  }

  // === mappedFuture の完了を待つ ===
  // mainスレッドを止めて、mappedFuture が完了するまで待つ
  // mappedFuture は、futureByPromise.map の処理まで終わった Future
  Await.ready(mappedFuture, 5000.millisecond)
}