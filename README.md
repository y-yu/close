Close Monad
==================

## How to use

```
$ sbt run
```

## Example codes

```scala
implicit def closer[R <: Closeable]: Closer[R] = Closer { x =>
  x.close()
}

(for {
  in     <- Close(new FileInputStream(getClass.getResource("/source.txt").getPath))
  reader <- Close(new InputStreamReader(in, "UTF-8"))
  buff   <- Close(new BufferedReader(reader))
  out    <- Close(new FileOutputStream("dest.txt"))
  writer <- Close(new OutputStreamWriter(out, "UTF-8"))
} yield {
  var line = buff.readLine()
  while (line != null) {
    println(line)

    writer.write(line + "\n")
    line = buff.readLine()
  }
}).run()
```