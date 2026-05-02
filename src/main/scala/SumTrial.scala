object SumTrial extends App {

  val length = 10000000
  // 1 から 1000 万までの Long のリストを作成する
  val list = (for (i <- 1 to length) yield i.toLong).toList

  // 処理開始時刻を記録する
  val start = System.currentTimeMillis()
  // リストの全要素を合計する
  val sum = list.sum
  // 処理にかかった時間を計算する
  val time = System.currentTimeMillis() - start

  println(sum)
  println(s"time: ${time} msec")
}