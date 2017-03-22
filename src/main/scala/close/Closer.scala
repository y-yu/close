package close

trait Closer[-A] {
  def close(a: A): Unit
}

object Closer {
  def apply[A](f: A => Unit): Closer[A] = new Closer[A] {
    def close(a: A): Unit = f(a)
  }
}