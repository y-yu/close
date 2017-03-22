import java.io.BufferedReader
import java.io.Closeable
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import close.Close
import close.Closer

object Main {
  def main(args: Array[String]): Unit = {
    implicit def closer[R <: Closeable]: Closer[R] = Closer(_.close())

    val a = for {
      in     <- Close(new FileInputStream("source.txt"))
      reader <- Close(new InputStreamReader(in, "UTF-8"))
      buff   <- Close(new BufferedReader(reader))
      out    <- Close(new FileOutputStream("dest.txt"))
      writer <- Close(new OutputStreamWriter(out, "UTF-8"))
    } yield {
      var line = buff.readLine()
      while (line != null) {
        writer.write(line)
        line = buff.readLine()
      }
      line
    }

    Close(new FileInputStream("source.txt")).flatMap { in =>
      Close(new InputStreamReader(in, "UTF-8")).map { reader =>
        ()
      }
    }
  }
}
