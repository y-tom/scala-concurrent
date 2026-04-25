import java.util.concurrent.atomic.AtomicReference

// === main処理（extends App） ===
object ReadModifyWrite extends App {
  // === 別スレッド（100個) ===
  for(i <- 1 to 100){
    new Thread(() => println(FactorialProvider.next)).start
  }
}

object FactorialProvider {
  // valなので参照は固定。ただしAtomicReferenceの中身はsetで変更できる
  private[this] val lastNumber = new AtomicReference[BigInt](BigInt(0)) // BigInt型を入れる箱、初期値はBigIntの0
  private[this] val lastFactorial = new AtomicReference[BigInt](BigInt(1))

  // get → 計算 → set が分かれているため、途中で他スレッドが割り込む
  // synchronized で get → 計算 → set をまとめてアトミックにする（ロック）
  def next: BigInt = synchronized { // アトミック化（synchronized）
    // 現在の数を取得し、100ms待って、1を足して次の番号を作成し、AtomicReferenceに格納する
    val currentNum = lastNumber.get() // read
    Thread.sleep(100)
    val nextNum = currentNum + 1 // modify
    lastNumber.set(nextNum) // write

    // 現在の階乗の数を取得し、100ms待って、次の数を現在の階乗に掛けて、状態を更新、値を返す
    val currentFact = lastFactorial.get() // read
    Thread.sleep(100)
    // 階乗計算（現在の階乗 × 次の数）
    val nextFact = currentFact * nextNum // modify
    lastFactorial.set(nextFact) // write
    nextFact
  }
}
