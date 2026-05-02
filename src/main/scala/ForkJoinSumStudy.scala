import java.util.concurrent.{ForkJoinPool, RecursiveTask}

object ForkJoinSumStudy extends App {

  val length = 10000000
  // 1 から 1000 万までの Long のリストを作成する
  val list = (for (i <- 1 to length) yield i.toLong).toList

  // Fork/Join のタスクを実行するプールを作成する
  val pool = new ForkJoinPool()

  // リストを分割しながら、合計を計算するタスク
  class AggregateTask(list: List[Long]) extends RecursiveTask[Long] {
    override def compute(): Long = {
      // リストを2分割するための位置を求める
      val n = list.length / 2
      if (n == 0) {
        // これ以上分割できない場合は、直接値を返す
        list match {
          case List() => 0
          case List(n) => n
        }
      } else {
        // リストを左右に分割する
        val (left, right) = list.splitAt(n)
        // 分割したリストから、それぞれタスクを作成する
        val leftTask = new AggregateTask(left)
        val rightTask = new AggregateTask(right)
        // タスクを非同期で実行する
        leftTask.fork()
        rightTask.fork()
        // それぞれの結果を取得して合計する
        leftTask.join() + rightTask.join()
      }
    }
  }

  // 処理開始時刻を記録する
  val start = System.currentTimeMillis()
  // ForkJoinPool でタスクを実行し、1 から 1000 万までの合計を計算する
  val sum = pool.invoke(new AggregateTask(list))
  // 処理にかかった時間を計算する
  val time = System.currentTimeMillis() - start

  println(sum)
  println(s"time: ${time} msec")
}