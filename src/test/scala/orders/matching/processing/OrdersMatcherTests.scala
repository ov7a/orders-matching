package orders.matching.processing

import orders.matching.processing.OrdersMatcher.matchOrders
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OrdersMatcherTests extends AnyFlatSpec with Matchers {
  private val validBuyOrder = Order(clientId = "C2", orderType = BuyOrderType, assetId = "A", price = 13, amount = 7)
  private val validSellOrder = Order(clientId = "C1", orderType = SellOrderType, assetId = "A", price = 13, amount = 7)

  private def checkMatch(order: Order, anotherOrder: Order, expectedResult: OrderMatch): Unit = {
    matchOrders(order, anotherOrder) shouldBe expectedResult
    matchOrders(anotherOrder, order) shouldBe expectedResult
  }

  "orders with the same client" should "be SameClientMatch" in {
    checkMatch(
      validBuyOrder,
      validSellOrder.copy(clientId = validBuyOrder.clientId),
      SameClientMatch
    )

    checkMatch(
      validBuyOrder,
      validBuyOrder,
      SameClientMatch
    )
  }

  "exact match" should "be FullMatch with sell price" in {
    checkMatch(
      validBuyOrder,
      validSellOrder,
      FullMatch(validSellOrder.price)
    )
  }

  "exact match with buy price higher than sell price" should "be FullMatch with sell price" in {
    checkMatch(
      validBuyOrder.copy(price = validSellOrder.price + 1),
      validSellOrder,
      FullMatch(validSellOrder.price)
    )
  }

  "exact match with buy price lower than sell price" should "be NoMatch" in {
    checkMatch(
      validBuyOrder.copy(price = validSellOrder.price - 1),
      validSellOrder,
      NoMatch
    )
  }

  "partial match" should "be PartialMatch with lowest amount" in {
    checkMatch(
      validBuyOrder.copy(amount = 3),
      validSellOrder,
      PartialMatch(price = validSellOrder.price, amount = 3)
    )

    checkMatch(
      validBuyOrder,
      validSellOrder.copy(amount = 3),
      PartialMatch(price = validSellOrder.price, amount = 3)
    )
  }

  "partial match with buy price higher than sell price" should "be PartialMatch with sell price" in {
    checkMatch(
      validBuyOrder.copy(amount = 3, price = validSellOrder.price + 1),
      validSellOrder,
      PartialMatch(price = validSellOrder.price, amount = 3)
    )

    checkMatch(
      validBuyOrder.copy(price = validSellOrder.price + 1),
      validSellOrder.copy(amount = 3),
      PartialMatch(price = validSellOrder.price, amount = 3)
    )
  }

  "partial match with buy price lower than sell price" should "be NoMatch" in {
    checkMatch(
      validBuyOrder.copy(amount = 3, price = validSellOrder.price - 1),
      validSellOrder,
      NoMatch
    )

    checkMatch(
      validBuyOrder.copy(price = validSellOrder.price - 1),
      validSellOrder.copy(amount = 3),
      NoMatch
    )
  }
}
