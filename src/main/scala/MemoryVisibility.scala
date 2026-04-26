// === mainスレッド ===
// readyフラグがtrueになるまで、別スレッドでwhile句を用いて待ち続け、
// readyがtrueになったらnumberの値を出力する
// スレッド間で同期が取れていないため、正しく動くときも正しく動かないときもある
object MemoryVisibility  extends App{
  // --- volatileで可視性を保証する ---
  @volatile var number = 0
  @volatile var ready = false

  // === 別スレッド ===
  new Thread (() => {
    while (!ready) {
      // Thread.yieldメソッド
      // 他の実行可能なスレッドがある場合は実行、ない場合は待つ
      Thread.`yield`()
    }
    println(number)
  }).start()

  number = 2525
  ready = true
}
