package close

import java.io.Closeable
import java.io.FileInputStream
import scalaprops.Gen
import scalaz.Equal
import scala.util.Failure
import scala.util.Success
import scala.util.Try

object CloseTestHelper {
  import scalaz.std.anyVal._

  implicit val genFileInputStream: Gen[FileInputStream] = Gen.value(new FileInputStream(getClass.getResource("/test.txt").getPath))

  case class Error(value: Int) extends Exception(s"message: ${value.toString}")

  implicit def genError(implicit I: Gen[Int]): Gen[Error] =
    I.map(x => Error(x))

  implicit def equalError(implicit I: Equal[Int]): Equal[Error] =
    I.contramap(_.value)

  implicit def equalTryError[A](implicit A: Equal[A], E: Equal[Error]): Equal[Try[A]] =
    Equal.equal {
      case (Success(va), Success(vb)) => A.equal(va, vb)
      case (Failure(ea@(Error(_))), Failure(eb@Error(_))) => E.equal(ea, eb)
      case _ => false
    }

  implicit def genCloser[A <: Closeable](implicit A: Gen[A]): Gen[Closer[A]] =
    A.map[Closer[A]] {
      r => new Closer[A] {
        def close(r: A): Unit = r.close()
      }
    }

  implicit def equalClose[A](implicit T: Equal[Try[A]], C: Gen[Closer[A]]): Equal[Close[A]] =
    T.contramap(_.run()(C.sample()))

  implicit def genClose[A](implicit A: Gen[A], E: Gen[Error]): Gen[Close[A]] =
    Gen.frequency(
      1 -> E.map[Close[A]] { e =>
        new Close[A] {
          def process(res): A = throw e
        }
      },
      2 -> A.map[Close[A]] { a =>
        new Close[A] {
          def process(): A = a
        }
      }
    )
}
