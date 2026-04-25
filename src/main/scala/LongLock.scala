import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator

// === main処理（extends App） ===
object LongLock extends App {
  // === 別スレッド（100個) ===
    for (i <- 1 to 100) {
      new Thread(() => println(NumAndCurrentDateProvider.next)).start()
    }
}

object NumAndCurrentDateProvider {
  private[this] val lastNumber = new AtomicReference[BigInt](BigInt(0))

  /*
  // nextメソッド全体をsynchronizedで囲んでいる
  // lastNumberの更新だけでなく、重い現在時刻取得もロック内で実行される
  def next :(BigInt,LocalDateTime) = synchronized{
    val nextNumber = lastNumber.updateAndGet(new UnaryOperator[BigInt] {
      override def apply(t: BigInt): BigInt = t + 1
    })
   */

  // lastNumberの更新処理のみをsynchronizedで囲む
  // 必要な最小限の処理だけをアトミックにする
  // 重い処理（currentDateSoHeavy）はロック外で実行する
  def next: (BigInt, LocalDateTime) = {
    val nextNumber = synchronized {
      lastNumber.updateAndGet(new UnaryOperator[BigInt] {
        override def apply(t: BigInt): BigInt = t + 1
      })
    }
    (nextNumber, currentDateSoHeavy)
  }

  // 現在時刻を取得する
  // 100msかかる重い処理
  def currentDateSoHeavy: LocalDateTime = {
    Thread.sleep(100)
    LocalDateTime.now()
  }
}