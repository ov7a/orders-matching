package orders.matching

import orders.matching.io.{BalancesParser, OrdersParser, Parser, BalancesWriter}
import orders.matching.processing._

object Main {
  def main(args: Array[String]): Unit = {
    val (clientsFileName, ordersFileName, resultFileName) = //TODO: named arguments using scallop
      if (args.length != 3) {
        println("Usage: sbt \"run clients.txt orders.txt result.txt\"")

        //for task purposes only, otherwise System.exit(1) should be here
        val baseDir = "src/main/resources/"
        (
          baseDir + "clients.txt",
          baseDir + "orders.txt",
          baseDir + "result.txt"
        )
      } else {
        (args(0), args(1), args(2))
      }

    val balances = BalancesParser.parse(clientsFileName)
    val orders = OrdersParser.parse(ordersFileName)

    val initialState = State(balances)

    val newState = OrdersProcessor.processAll(initialState, orders)

    BalancesWriter.writeResult(newState.balances, resultFileName)
  }
}
