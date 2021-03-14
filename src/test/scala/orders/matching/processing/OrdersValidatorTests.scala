package orders.matching.processing

import orders.matching.processing.OrdersValidator.validate
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class OrdersValidatorTests extends AnyFlatSpec with Matchers {
  private val balances = Map(
    "C1" -> BalanceState(
      balance = 1000,
      assets = Map("A" -> 100)
    )
  )

  private val validOrder = Order(clientId = "C1", orderType = SellOrderType, assetId = "A", price = 1, amount = 1)

  "valid order" should "be valid" in {
    val result = validate(
      validOrder,
      balances
    )
    result shouldBe true
  }

  "order with negative price" should "be invalid" in {
    val result = validate(
      validOrder.copy(price = -1),
      balances
    )
    result shouldBe false
  }

  "order with zero price" should "be invalid" in {
    val result = validate(
      validOrder.copy(price = 0),
      balances
    )
    result shouldBe false
  }

  "order with negative amount" should "be invalid" in {
    val result = validate(
      validOrder.copy(amount = -1),
      balances
    )
    result shouldBe false
  }

  "order with zero amount" should "be invalid" in {
    val result = validate(
      validOrder.copy(amount = 0),
      balances
    )
    result shouldBe false
  }

  "buy order with insufficient funds" should "be invalid" in {
    val result = validate(
      validOrder.copy(orderType = BuyOrderType, price = 100, amount = 11),
      balances
    )
    result shouldBe false
  }

  "sell order with insufficient amount" should "be invalid" in {
    val result = validate(
      validOrder.copy(amount = 101),
      balances
    )
    result shouldBe false
  }

  "sell order for non-existing asset" should "be invalid" in {
    val result = validate(
      validOrder.copy(assetId = "B"),
      balances
    )
    result shouldBe false
  }
}
