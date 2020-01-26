package orders.matching.io

import java.io.{BufferedReader, FileReader}

import scala.util.Using

object Parser { //TODO: error handling
  def parseLines[T](fileName: String, parse: String => Option[T]): Iterator[T] = {
    Using.resource(new BufferedReader(new FileReader(fileName))) { reader => //TODO: iterator out of scope
      Iterator.continually(reader.readLine())
              .takeWhile(_ != null)
              .map(_.trim)
              .filter(_.nonEmpty)
              .flatMap(parse)
    }
  }
}
