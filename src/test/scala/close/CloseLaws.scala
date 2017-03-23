package close

import scala.util.Success
import scala.util.Try

object CloseLaws {
  def oneCloseLaw[R <: TestCloseable](c: Close[R, R], closer: TestCloser[R]): Boolean =
    Try(c.run()(closer)) match {
      case Success(r) => closer.closedOrder.length == 1
      case _ => true
    }

  def orderCloseLaw[R <: TestCloseable, A](res1: R, res2: R, closer: TestCloser[R]): Boolean = {
    (for {
      a <- Close(res1)
      b <- Close(res2)
    } yield ()).run()(closer)

    closer.closedOrder == Seq(res2, res1)
  }
}
