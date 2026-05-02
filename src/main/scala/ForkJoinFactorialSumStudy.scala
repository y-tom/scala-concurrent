import java.util.concurrent.{ForkJoinPool, RecursiveTask}
import scala.annotation.tailrec

object ForkJoinFactorialSumStudy extends App {
  val length = 5000
  // 1 から 5000 までの BigInt のリストを作成する
  val list = (for (i <- 1 to length) yield BigInt(i)).toList

  // Fork/Join のタスクを実行するプールを作成する
  val pool = new ForkJoinPool()

  // リストを分割しながら、階乗の合計を計算するタスク
  class AggregateTask(list: List[BigInt]) extends RecursiveTask[BigInt] {
    override def compute(): BigInt = {
      // リストを2分割するための位置を求める
      val n = list.length/2

      if(n == 0){
        // これ以上分割できない場合は、直接計算する
        list match{
          case List() => 0
          case List(n) => factorial(n, BigInt(1))
        }
      } else {
        // リストを左右に分割する
        val (left,right) =list.splitAt(n)
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

    // n! を計算する
    @tailrec
    private[this] def factorial(i: BigInt, acc: BigInt): BigInt =
      if (i == 0) acc else factorial(i - 1, i * acc)
  }

  // 処理開始時刻を記録する
  val start = System.currentTimeMillis()
  // ForkJoinPool でタスクを実行し、1! + 2! + 3! + ... + 5000! を計算する
  val factorialSum = pool.invoke(new AggregateTask(list))
  // 処理にかかった時間を計算する
  val time = System.currentTimeMillis() - start

  println(factorialSum)
  println(s"time: ${time} msec")
}
