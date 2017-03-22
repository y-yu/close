package close

import java.io.FileInputStream
import scalaprops._

object CloseTest extends Scalaprops {
  import scalaz._
  import scalaz.std.anyVal._
  import CloseTestHelper._

  implicit val closeMonadInstance = new Monad[Close] {
    def point[A](a: => A): Close[A] = Close(a)
    def bind[A, B](a: Close[A])(f: A => Close[B]): Close[B] = a.flatMap(f)
  }

  implicit val fileInputCloser: Closer[FileInputStream] = new Closer[FileInputStream] {
    override def close(r: FileInputStream): Unit = r.close()
  }

  val fileInputCloseTest = Properties.list(
    scalazlaws.monad.all[Close]
  )
}
