package close

import java.io.FileInputStream
import scalaprops._

object CloseTest extends Scalaprops {
  import scalaz._
  import scalaz.std.anyVal._
  import CloseTestHelper._

  implicit val fileInputStream: FileInputStream = new FileInputStream(getClass.getResource("/test.txt").getPath)

  implicit def closeMonadInstance[R, A](implicit r: R) = new Monad[({type L[B] = Close[R, B]})#L] {
    def point[B](a: => B): Close[R, B] = Close[R, B](r, a)
    def bind[B, C](a: Close[R, B])(f: B => Close[R, C]): Close[R, C] = a.flatMap(f)
  }

  implicit val fileInputCloser: Closer[FileInputStream] = new Closer[FileInputStream] {
    override def close(r: FileInputStream): Unit = r.close()
  }

  type FileInputStreamClose[A] = Close[FileInputStream, A]

  val fileInputCloseTest = Properties.list(
    scalazlaws.monad.all[FileInputStreamClose]
  )
}
