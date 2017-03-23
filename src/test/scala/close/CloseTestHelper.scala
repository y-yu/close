package close

import scala.collection.mutable.ArrayBuffer
import scalaprops.Gen
import scalaz.Equal
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object CloseTestHelper {
  case class Error(value: Int) extends Exception(s"message: ${value.toString}")

  implicit def genError(implicit I: Gen[Int]): Gen[Error] =
    I.map(x => Error(x))

  implicit def equalError(implicit I: Equal[Int]): Equal[Error] =
    I.contramap(_.value)

  implicit def genTestCloseable(implicit I: Gen[Int]): Gen[TestCloseable] =
    Gen.frequency(
      1 -> I.map(i => new ConcreteTestCloseable1(i)),
      2 -> I.map(i => new ConcreteTestCloseable2(i))
    )

  implicit def equalTestCloseable(implicit I: Equal[Int]): Equal[TestCloseable] =
    I.contramap(_.id)

  implicit def equalArrayBuffer[A](implicit A: Equal[A]): Equal[ArrayBuffer[A]] =
    Equal.equal { (a, b) =>
      (a zip b) forall {
        case (x, y) => A.equal(x, y)
      }
    }

  implicit def genCloser[A](implicit A: Gen[A]): Gen[TestCloser[A]] =
    A.map[TestCloser[A]](_ => new TestCloser[A])

  implicit def equalClose[R, A](implicit A: Equal[A], E: Equal[Error], C: Gen[TestCloser[R]], B: Equal[ArrayBuffer[R]]): Equal[Close[R, A]] =
    Equal.equal { (a, b) =>
      val c1 = C.sample()
      val c2 = C.sample()
      (Try(a.run()(c1)), Try(b.run()(c2))) match {
        case (Success(va), Success(vb)) => A.equal(va, vb) && B.equal(c1.closedOrder, c2.closedOrder)
        case (Failure(ea@(Error(_))), Failure(eb@Error(_))) => E.equal(ea, eb) && B.equal(c1.closedOrder, c2.closedOrder)
        case _ => false
      }
    }

  implicit def genClose[R, A](implicit A: Gen[A], E: Gen[Error], R: Gen[R]): Gen[Close[R, A]] =
    Gen.frequency(
      1 -> E.map[Close[R, A]] { e =>
        new Close[R, A](R.sample()) {
          def process()(implicit closer: Closer[R]): A = throw e
        }
      },
      2 -> A.map[Close[R, A]] { a =>
        new Close[R, A](R.sample()) {
          def process()(implicit closer: Closer[R]): A = a
        }
      }
    )
}
