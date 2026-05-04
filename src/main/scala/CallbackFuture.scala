import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}
import scala.util.Random

// === callback形式の処理を持つクラス ===
class CallbackSomething {
  val random = new Random()

  // 成功時の処理 onSuccess と、失敗時の処理 onFailure を受け取る
  // このメソッド自体は Future を返さない
  def doSomething(onSuccess: Int => Unit, onFailure: Throwable => Unit): Unit = {
    val i = random.nextInt(10)
    // 5未満なら成功コールバックを呼ぶ
    if (i < 5) onSuccess(i)
    // 5以上なら失敗コールバックを呼ぶ
    else onFailure(new RuntimeException(i.toString))
  }
}


// === callback形式の処理を Future に変換するクラス ===
class FutureSomething {
  val callbackSomething = new CallbackSomething

  def doSomething(): Future[Int] = {
    // Promise は「結果を入れる側」
    val promise = Promise[Int]
    // callback形式の処理を呼び出す
    callbackSomething.doSomething(
      // 成功したら Promise に成功値を入れる
      i => promise.success(i),
      // 失敗したら Promise に失敗情報を入れる
      t => promise.failure(t)
    )

    // Promise から Future を返す
    // 呼び出し側は callback ではなく Future として扱える
    promise.future
  }
}


// === Future として使う側 ===

object CallbackFuture extends App {

  // === callback処理をFuture化したクラスを使う ===

  val futureSomething = new FutureSomething

  // doSomething() は Future[Int] を返す
  val iFuture = futureSomething.doSomething()
  val jFuture = futureSomething.doSomething()

  // === for式で2つのFutureを組み合わせる ===

  // 両方成功したときだけ i + j が作られる
  val iplusj: Future[Int] = for {
    i <- iFuture
    j <- jFuture
  } yield i + j

  // === 結果を待って表示する ===

  // 成功すれば合計値が返る
  // どちらかが失敗すれば RuntimeException が発生する
  val result = Await.result(iplusj, Duration.Inf)

  println(result)
}