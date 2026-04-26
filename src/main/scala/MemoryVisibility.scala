// === mainスレッド ===
// readyフラグがtrueになるまで、別スレッドでwhile句を用いて待ち続け、
// readyがtrueになったらnumberの値を出力する
// スレッド間で同期が取れていないため、正しく動くときも正しく動かないときもある
object MemoryVisibility  extends App{
  // --- synchronizedで可視性とアトミック性を保証する ---
  var number = 0
  var ready = false
  // 読み取り側を同期
  private[this] def getNumber: Int = synchronized{number}
  private[this] def getReady: Boolean = synchronized{ready}

  // === 別スレッド ===
  new Thread (() => {
    while (!getReady) {
      // Thread.yieldメソッド
      // 他の実行可能なスレッドがある場合は実行、ない場合は待つ
      Thread.`yield`()
    }
    println(getNumber)
  }).start()

  // 書き込み側を同期
  synchronized{
    number = 2525
    ready = true
  }
}
