package close

abstract class Close[+R, +A](res: R) { self =>
  def process()(implicit closer: Closer[R]): A

  def run()(implicit closer: Closer[R]): A =
    self.process()

  def flatMap[AR >: R, B](f: A => Close[AR, B]): Close[AR, B] = new Close[AR, B](res) {
    def process()(implicit closer: Closer[AR]): B =
      try {
        f(self.process()).process()
      } finally {
        closer.close(res)
      }
  }

  def map[B](f: A => B): Close[R, B] = flatMap(x => Close(res, f(x)))
}

object Close {
  def apply[R, A](res: R, a: => A) = new Close[R, A](res) {
    def process()(implicit closer: Closer[R]): A = a
  }

  def apply[R](r: R): Close[R, R] = apply(r, r)
}