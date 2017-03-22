package close

trait Close[R, A] { self =>
  def process(res: A): A

  def flatMap[B](f: A => Close[R, B]): Close[R, B] = new Close[R, B] {
    def process(res: B): B =
      try {
        f(self.process(res)).process(res)
      } finally {
        closer.close(res)
      }
  }

  def map[B](f: A => B): Close[B] = flatMap(x => Close(f(x)))
}

object Close {
  def apply[A](a: => A): Close[A] = new Close[A] {
    def process[R](res: R)(implicit closer: Closer[R]): A = a
  }
}