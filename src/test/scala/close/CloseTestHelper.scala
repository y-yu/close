package close

import java.io.Closeable
import java.io.FileInputStream
import scalaprops.Gen
import scalaz.Equal
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object CloseTestHelper {
  implicit val genFileInputStream: Gen[FileInputStream] = Gen.value(new FileInputStream(getClass.getResource("/test.txt").getPath))

  case class Error(value: Int) extends Exception(s"message: ${value.toString}")

  implicit def genError(implicit I: Gen[Int]): Gen[Error] =
    I.map(x => Error(x))

  implicit def equalError(implicit I: Equal[Int]): Equal[Error] =
    I.contramap(_.value)

  implicit def genCloser[A <: Closeable](implicit A: Gen[A]): Gen[Closer[A]] =
    A.map[Closer[A]] {
      r => new Closer[A] {
        def close(r: A): Unit = r.close()
      }
    }

  implicit def equalClose[R, A](implicit A: Equal[A], E: Equal[Error], C: Gen[Closer[R]]): Equal[Close[R, A]] =
    Equal.equal { (a, b) =>
      val c = C.sample()
      (Try(a.run()(c)), Try(b.run()(c))) match {
        case (Success(va), Success(vb)) => A.equal(va, vb)
        case (Failure(ea@(Error(_))), Failure(eb@Error(_))) => E.equal(ea, eb)
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
