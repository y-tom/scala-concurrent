object QuadNumberPrinter {
  private var counter = 0

  def next(): Int = synchronized{
    counter = counter + 1
    counter
  }
  for (i <- 1 to 4){
    new Thread(() => for (j <- 1 to 100000) println(s"thread ${i}: ${j}") ).start()
  }
}
