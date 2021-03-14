package orders.matching

import orders.matching.io.{BalancesParser, BalancesWriter, OrdersParser}
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
    val ordersTransformer = OrdersParser.parse[State](ordersFileName)

    val initialState = State(balances)

    val newState = ordersTransformer.apply(OrdersProcessor.processAll(initialState, _))

    BalancesWriter.writeResult(newState.balances, resultFileName)
  }
}
