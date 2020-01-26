package orders.matching.io

import java.io.{File, PrintWriter}

import orders.matching.processing.Balances

import scala.util.Using

object BalancesWriter {
  def writeResult(balances: Balances, resultFileName: String): Unit = {
    Using(new PrintWriter(new File(resultFileName))) { writer =>
      balances.foreach{ case (clientId, balanceState) =>
        val parts = Seq(clientId, balanceState.balance.toString) ++
          balanceState.assets //TODO: how to sort?
        writer.write(parts.mkString("\t"))
      }
    }
  }
}
