import java.io._
import close.Close
import close.Closer

object Main {
  def main(args: Array[String]): Unit = {
    implicit def closer[R <: Closeable]: Closer[R] = Closer { x =>
      println(s"close: ${x.toString}")
      x.close()
    }

    val a = for {
      in     <- Close(new FileInputStream(getClass.getResource("/source.txt").getPath))
      reader <- Close(new InputStreamReader(in, "UTF-8"))
      buff   <- Close(new BufferedReader(reader))
      out    <- Close(new FileOutputStream("dest.txt"))
      writer <- Close(new OutputStreamWriter(out, "UTF-8"))
    } yield {
      println("begin")

      var line = buff.readLine()
      while (line != null) {
        println(line)

        writer.write(line + "\n")
        line = buff.readLine()
      }

      println("end")
    }

    a.run()
  }
}
