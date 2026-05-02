import scala.annotation.tailrec

object FactorialSumTrial extends App {
  val length = 5000
  // 1 から 5000 までの BigInt のリストを作成する
  val list = (for (i <- 1 to length) yield BigInt(i)).toList

  // n! を計算する
  @tailrec
  private[this] def factorial(i: BigInt, acc: BigInt): BigInt =
    if (i == 0) acc else factorial(i - 1, i * acc)

  // 処理開始時刻を記録する
  val start = System.currentTimeMillis()
  // 1! + 2! + 3! + ... + 5000! を計算する
  val factorialSum = list.foldLeft(BigInt(0))((acc, n) => acc + factorial(n, 1))
  // 処理にかかった時間を計算する
  val time = System.currentTimeMillis() - start

  println(factorialSum)
  println(s"time: ${time} msec")
}