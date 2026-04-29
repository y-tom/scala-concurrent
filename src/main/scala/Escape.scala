import scala.collection.mutable.ArrayBuffer

object Escape extends App{
  for (i <- 1 to 100){
    new Thread(() => {
      println(EscapeVarSeqProvider.next)
      println(EscapeArrayBufferProvider.next)
    }).start()
  }
}

object EscapeVarSeqProvider {
  // seqフィールドがpublicフィールドになっており、別なスレッドからアクセス可能
  // seqのアクセス修飾子を private[this] とすることで正しくカプセル化
  private[this] var seq: Seq[Int] = Seq()
  def next: Seq[Int] = synchronized{
    val nextSeq = seq :+ (seq.size + 1)
    seq = nextSeq
    nextSeq
  }
}

object EscapeArrayBufferProvider {
  private[this] val array: ArrayBuffer[Int] = ArrayBuffer.empty[Int]
  def next: ArrayBuffer[Int] = synchronized{
    array += (array.size + 1)
    // フィールドのarrayではprivate[this]で他のオブジェクトから参照できないようになっている
    // しかしnextメソッド自身が可変オブジェクトのインスタンスを返している
    // arrayインスタンスそのものではなく arrayインスタンスのコピーをnextメソッドで返却するようにした
    // 内部のフィールドの参照が逸出することはないため、スレッドセーフ
    array.clone
  }
}