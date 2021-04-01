package orders.matching.io

import orders.matching.processing.Balances
import org.scalajs.dom.document
import org.scalajs.dom.html.TextArea

object BalancesWriter {
  def writeResult(balances: Balances, outputId: String): Unit = {
    val builder = new StringBuilder()

    balances.toSeq.sortBy(_._1).foreach { case (clientId, balanceState) =>
      val parts = Seq(clientId, balanceState.balance.toString) ++
        balanceState.assets.toSeq.sortBy(_._1).map(_._2)
      builder.append(parts.mkString(start = "", sep = "\t", end = "\n"))
    }

    document.getElementById(outputId).asInstanceOf[TextArea].value = builder.toString()
  }
}
