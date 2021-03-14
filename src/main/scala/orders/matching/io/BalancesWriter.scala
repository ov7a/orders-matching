package orders.matching.io

import java.io.{File, PrintWriter}

import orders.matching.processing.Balances

import scala.util.Using

object BalancesWriter {
  def writeResult(balances: Balances, resultFileName: String): Unit = {
    Using(new PrintWriter(new File(resultFileName))) { writer =>
      balances.toSeq.sortBy(_._1).foreach { case (clientId, balanceState) =>
        val parts = Seq(clientId, balanceState.balance.toString) ++
          balanceState.assets.toSeq.sortBy(_._1).map(_._2)
        writer.write(parts.mkString(start = "", sep = "\t", end = "\n"))
      }
    }
  }
}
