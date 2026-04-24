// === mainスレッド ===
object ThreadStudy extends App {
  // Thread.currentThread()メソッド　現在の処理が実行しているスレッドのインスタンスを取得するメソッド 
  println(Thread.currentThread().getName)

  // === 別スレッド ===
  val thread = new Thread(() => {
    // 1000ms待たせる
    Thread.sleep(1000)
    // printInで現在実行しているスレッドの名前をコンソールに出力
    println(Thread.currentThread().getName)
  })
  // スレッド開始（別スレッドで処理が走る）
  thread.start()

  println("main thread finished.")
}
