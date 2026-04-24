// === mainスレッド ===
object ThreadRisk extends App {

  private var counter = 0

  // synchronizedブロックで排他制御
  def next(): Int = synchronized{
    counter = counter + 1
    counter
  }

  // === 別スレッド10個 ===
  for (i <- 1 to 10) {
    new Thread(() => for(j <- 1 to 100000)  println(next())).start()
  }
}