package close

import java.io.Closeable
import scalaprops.Property.forAll
import scalaprops._

trait TestCloseable extends Closeable {
  val id: Int
  def close(): Unit = ()
}

class ConcreteTestCloseable1(val id: Int) extends TestCloseable

class ConcreteTestCloseable2(val id: Int) extends TestCloseable

class TestCloser[A] extends Closer[A] {
  val closedOrder: scala.collection.mutable.ArrayBuffer[A] = scala.collection.mutable.ArrayBuffer.empty[A]

  def close(r: A): Unit = closedOrder.synchronized {
    closedOrder += r
  }
}

object CloseTest extends Scalaprops {
  import scalaz._
  import scalaz.std.anyVal._
  import CloseTestHelper._

  implicit def closeMonadInstance[R, A](implicit r: Gen[R]) = new Monad[({type L[B] = Close[R, B]})#L] {
    def point[B](a: => B): Close[R, B] = Close[R, B](r.sample(), a)
    def bind[B, C](a: Close[R, B])(f: B => Close[R, C]): Close[R, C] = a.flatMap(f)
  }

  implicit val closer: Closer[TestCloseable] = new TestCloser[TestCloseable]

  type TestCloseableClose[A] = Close[TestCloseable, A]

  val closeMonadTest = Properties.list(
    scalazlaws.monad.all[TestCloseableClose]
  )

  val oneCloseLawTest = forAll(CloseLaws.oneCloseLaw[TestCloseable] _)

  val orderCloseLawTest = forAll(CloseLaws.orderCloseLaw[TestCloseable, Int] _)
}
