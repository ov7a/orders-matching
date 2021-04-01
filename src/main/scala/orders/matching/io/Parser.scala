package orders.matching.io

import java.io.{BufferedReader, StringReader}

import org.scalajs.dom.document
import org.scalajs.dom.html.TextArea

import scala.util.Using

object Parser {
  val separator = "\\s+"

  def parseLines[T, R](
    textAreaId: String,
    parse: String => Option[T]
  )(
    process: Iterator[T] => R
  ): R = {
    val text = document.getElementById(textAreaId).asInstanceOf[TextArea].value

    Using.resource(new BufferedReader(new StringReader(text))) { reader =>
      process(
        Iterator.continually(reader.readLine())
                .takeWhile(_ != null)
                .map(_.trim)
                .filter(_.nonEmpty)
                .flatMap(parse)
      )
    }
  }
}
