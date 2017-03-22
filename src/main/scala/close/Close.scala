package close

abstract class Close[+R, +A](res: R) { self =>
  def run()(implicit closer: Closer[R]): A

  def flatMap[AR >: R, B](f: A => Close[AR, B]): Close[AR, B] = new Close[AR, B](res) {
    def run()(implicit closer: Closer[AR]): B =
      try {
        f(self.run()).run()
      } finally {
        closer.close(res)
      }
  }

  def map[B](f: A => B): Close[R, B] = flatMap(x => Close(res, f(x)))
}

object Close {
  def apply[R, A](res: R, a: => A) = new Close[R, A](res) {
    def run()(implicit closer: Closer[R]): A = a
  }

  def apply[R](r: R): Close[R, R] = apply(r, r)
}