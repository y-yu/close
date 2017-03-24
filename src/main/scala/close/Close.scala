package close

abstract class Close[+R, +A](res: R) { self =>
  protected def process()(implicit closer: Closer[R]): A

  def run()(implicit closer: Closer[R]): A =
    try {
      process()
    } finally {
      closer.close(res)
    }

  def flatMap[AR >: R, B](f: A => Close[AR, B]): Close[AR, B] = new Close[AR, B](res) {
    def process()(implicit closer: Closer[AR]): B =
      try {
        f(self.process()).process()
      } finally {
        closer.close(res)
      }

    override def run()(implicit closer: Closer[AR]): B =
      process()
  }

  def map[B](f: A => B): Close[R, B] = flatMap(x => Close(res, f(x)))
}

object Close {
  def apply[R, A](res: R, a: => A) = new Close[R, A](res) {
    def process()(implicit closer: Closer[R]): A = a
  }

  def apply[R](res: R): Close[R, R] = apply(res, res)
}