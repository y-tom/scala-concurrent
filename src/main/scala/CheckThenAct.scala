// === main処理（extends App） ===
object CheckThenAct extends App {
  // === 別スレッド（100個) ===
  // 100個のスレッドがSingletonProviderから単一のインスタンスを取得し、コンソールに出力
  for (i <- 1 to 100){
    new Thread(() => println(SingletonProvider.get)).start()
  }
}

// --- 遅延初期化 ---
// SingletonProviderオブジェクトが
// 単一のBigObjectインスタンスを生成して返し、
// 既に作成済みならそれを返す
object SingletonProvider {
  private[this] var singleton: BigObject = null

  // check（nullか確認）→ act（newする）が分かれているため、途中で他スレッドが割り込む
  // singletonがnullなら、BigObject型のインスタンスを作成し、フィールドに代入して、その値を返す
  // check（nullか確認）→ act（newして代入）をまとめてアトミックにする
  // synchronizedでロックし、他スレッドの割り込みを防ぐ
  // singletonがnullならBigObjectを作成し、作成済みなら同じインスタンスを返す
  def get: BigObject = this.synchronized {
    if (singleton == null){
      singleton = new BigObject()
    }
    singleton
  }
}

// インスタンス化するときには1000msかかる
class BigObject() {
  Thread.sleep(1000)
}
