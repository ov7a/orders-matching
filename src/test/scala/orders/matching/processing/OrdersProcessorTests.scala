package orders.matching.processing


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OrdersProcessorTests extends AnyFlatSpec with Matchers {

  private val balances = Map(
    "C1" -> BalanceState(100, Map("A" -> 10, "B" -> 15)),
    "C2" -> BalanceState(100, Map.empty)
  )

  private val buyOrder = Order("C2", BuyOrderType, "A", amount = 10, price = 3)
  private val sellOrder = Order("C1", SellOrderType, "A", amount = 10, price = 3)

  "exact matching" should "be done correctly" in {
    val orders = Map(
      "A" -> Vector(buyOrder)
    )

    val newState = OrdersProcessor.process(State(balances, orders), sellOrder)

    newState shouldBe Success(
      State(
        activeOrders = Map("A" -> Vector.empty),
        balances = Map(
          "C1" -> BalanceState(130, Map("A" -> 0, "B" -> 15)),
          "C2" -> BalanceState(70, Map("A" -> 10))
        )
      )
    )
  }

  "exact match with existing" should "be skipped in case of price mismatch" in {
    val orders = Map(
      "A" -> Vector(buyOrder)
    )
    val newOrder = sellOrder.copy(price = 4)
    val newState = OrdersProcessor.process(State(balances, orders), newOrder)

    newState shouldBe Success(
      State(
        activeOrders = Map("A" -> Vector(buyOrder, newOrder)),
        balances = balances
      )
    )
  }

  "exact matching with different prices" should "be done correctly" in {
    val orders = Map(
      "A" -> Vector(buyOrder.copy(price = 4))
    )
    val newState = OrdersProcessor.process(State(balances, orders), sellOrder)

    newState shouldBe Success(
      State(
        activeOrders = Map("A" -> Vector.empty),
        balances = Map(
          "C1" -> BalanceState(130, Map("A" -> 0, "B" -> 15)),
          "C2" -> BalanceState(70, Map("A" -> 10))
        )
      )
    )
  }

  "partial matching with partial match of new order" should "be done correctly" in {
    val orders = Map(
      "A" -> Vector(buyOrder)
    )
    val newOrder = sellOrder.copy(amount = 3)
    val newState = OrdersProcessor.process(State(balances, orders), newOrder)

    newState shouldBe Success(
      State(
        activeOrders = Map("A" -> Vector(buyOrder.copy(amount = 7))),
        balances = Map(
          "C1" -> BalanceState(109, Map("A" -> 7, "B" -> 15)),
          "C2" -> BalanceState(91, Map("A" -> 3))
        )
      )
    )
  }

  "partial matching  with partial match of existing order" should "be done correctly" in {
    val existingOrder = sellOrder.copy(amount = 3)

    val orders = Map(
      "A" -> Vector(existingOrder)
    )
    val newState = OrdersProcessor.process(State(balances, orders), buyOrder)

    newState shouldBe Success(
      State(
        activeOrders = Map("A" -> Vector(buyOrder.copy(amount = 7))),
        balances = Map(
          "C1" -> BalanceState(109, Map("A" -> 7, "B" -> 15)),
          "C2" -> BalanceState(91, Map("A" -> 3))
        )
      )
    )
  }

  "partial matching" should "be skipped in case of price mismatch" in {
    val existingOrder = sellOrder.copy(amount = 3, price = 4)

    val orders = Map(
      "A" -> Vector(existingOrder)
    )
    val newState = OrdersProcessor.process(State(balances, orders), buyOrder)

    newState shouldBe Success(
      State(
        activeOrders = Map("A" -> Vector(existingOrder, buyOrder)),
        balances = balances
      )
    )
  }

  "processor" should "skip unmatched orders" in {
    val orderToSkip = sellOrder.copy(price = 100)

    val orders = Map(
      "A" -> Vector(
        orderToSkip,
        sellOrder,
        orderToSkip
      )
    )
    val newState = OrdersProcessor.process(State(balances, orders), buyOrder)

    newState shouldBe Success(
      State(
        activeOrders = Map("A" -> Vector(orderToSkip, orderToSkip)),
        balances = Map(
          "C1" -> BalanceState(130, Map("A" -> 0, "B" -> 15)),
          "C2" -> BalanceState(70, Map("A" -> 10))
        )
      )
    )
  }

  "orders from the same client" should "not be matched" in {
    val orders = Map(
      "A" -> Vector(sellOrder)
    )

    val newOrder = buyOrder.copy(clientId = sellOrder.clientId)
    val newState = OrdersProcessor.process(State(balances, orders), newOrder)

    newState shouldBe Success(
      State(
        activeOrders = Map("A" -> Vector(sellOrder, newOrder)),
        balances = balances
      )
    )
  }

  "invalid orders" should "be deleted lazily" in {
    val hugeBuyOrder = buyOrder.copy(amount = 200)
    val hugeBuyOrder2 = buyOrder.copy(amount = 200, price = buyOrder.price + 1)

    val orders = Map(
      "A" -> Vector(
        hugeBuyOrder,
        buyOrder,
        hugeBuyOrder2
      )
    )

    val newState = OrdersProcessor.process(State(balances, orders), sellOrder)

    newState shouldBe Success(
      State(
        activeOrders = Map("A" -> Vector(hugeBuyOrder2)),
        balances = Map(
          "C1" -> BalanceState(130, Map("A" -> 0, "B" -> 15)),
          "C2" -> BalanceState(70, Map("A" -> 10))
        )
      )
    )
  }

  "all invalid orders" should "be deleted in NoMatch case" in {
    val hugeBuyOrder = buyOrder.copy(amount = 200)
    val hugeBuyOrder2 = buyOrder.copy(amount = 200, price = buyOrder.price + 1)

    val orders = Map(
      "A" -> Vector(hugeBuyOrder, hugeBuyOrder, buyOrder, hugeBuyOrder2, hugeBuyOrder2)
    )
    val newOrder = sellOrder.copy(price = 4)
    val newState = OrdersProcessor.process(State(balances, orders), newOrder)

    newState shouldBe Success(
      State(
        activeOrders = Map("A" -> Vector(buyOrder, newOrder)),
        balances = balances
      )
    )
  }

  "main method" should "process all valid orders and ignore all invalid ones" in {
    val newOrders = Seq(
      buyOrder,
      buyOrder.copy(amount = 200, clientId = "C2"),
      buyOrder.copy(price = -1),
      sellOrder
    )

    val newState = OrdersProcessor.processAll(State(balances, Map.empty), newOrders.iterator)

    newState shouldBe
      State(
        activeOrders = Map("A" -> Vector.empty),
        balances = Map(
          "C1" -> BalanceState(130, Map("A" -> 0, "B" -> 15)),
          "C2" -> BalanceState(70, Map("A" -> 10))
        )
      )
  }

}
