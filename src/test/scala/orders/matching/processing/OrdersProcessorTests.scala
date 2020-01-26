package orders.matching.processing


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OrdersProcessorTests extends AnyFlatSpec with Matchers {

  "extact matching" should "be done" in {
    val balances = Map(
      "C1" -> BalanceState(100, Map("A" -> 10, "B" -> 15)),
      "C2" -> BalanceState(100, Map("A" -> 0, "B" -> 0))
    )

    val existingOrder = Order("C1", BuyOrderType, "A", amount = 10, price = 3)

    val orders = Map(
      "A" -> List(existingOrder)
    )
    val newOrder = existingOrder.copy(orderType = SellOrderType, clientId = "C2")
    val newState = OrdersProcessor.process(State(balances, orders), newOrder)

    newState shouldBe Success(
      State(
        activeOrders = Map.empty,
        balances =  Map(
          "C1" -> BalanceState(100, Map("A" -> 10, "B" -> 15)),
          "C2" -> BalanceState(100, Map("A" -> 0, "B" -> 0))
        )
      )
    )
  }


}
