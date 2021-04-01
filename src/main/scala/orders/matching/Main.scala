package orders.matching

import orders.matching.io.{BalancesParser, BalancesWriter, OrdersParser}
import orders.matching.processing._

import scala.scalajs.js.annotation.JSExportTopLevel

object Main {

  @JSExportTopLevel("main")
  def main(): Unit = {
    val balances = BalancesParser.parse("clients")
    val ordersTransformer = OrdersParser.parse[State]("orders")

    val initialState = State(balances)

    val newState = ordersTransformer.apply(OrdersProcessor.processAll(initialState, _))

    BalancesWriter.writeResult(newState.balances, "result")
  }

}
