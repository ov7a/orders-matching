package orders.matching.processing

import orders.matching.processing.BalancesUpdater.updateBalances
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BalancesUpdaterTests extends AnyFlatSpec with Matchers {
  private val balances = Map(
    "C1" -> BalanceState(
      balance = 1000,
      assets = Map("A" -> 100)
    ),
    "C2" -> BalanceState(
      balance = 500,
      assets = Map("B" -> 100)
    )
  )

  private val validBuyOrder = Order(clientId = "C2", orderType = BuyOrderType, assetId = "A", price = 13, amount = 7)
  private val validSellOrder = Order(clientId = "C1", orderType = SellOrderType, assetId = "A", price = 13, amount = 7)

  "balances" should "not be changed if no orders are passed" in {
    updateBalances(balances, Seq.empty) shouldBe balances
  }

  "balances" should "be updated correctly for single buy order" in {
    updateBalances(balances, Seq(validBuyOrder)) shouldBe Map(
      "C1" -> BalanceState(
        balance = 1000,
        assets = Map("A" -> 100)
      ),
      "C2" -> BalanceState(
        balance = 409,
        assets = Map(
          "A" -> 7,
          "B" -> 100
        )
      )
    )
  }

  "balances" should "be updated correctly for single buy order if asset already exists" in {
    updateBalances(balances, Seq(validBuyOrder.copy(clientId = "C1"))) shouldBe Map(
      "C1" -> BalanceState(
        balance = 909,
        assets = Map("A" -> 107)
      ),
      "C2" -> BalanceState(
        balance = 500,
        assets = Map("B" -> 100)
      )
    )
  }

  "balances" should "be updated correctly for single sell order" in {
    updateBalances(balances, Seq(validSellOrder)) shouldBe Map(
      "C1" -> BalanceState(
        balance = 1091,
        assets = Map("A" -> 93)
      ),
      "C2" -> BalanceState(
        balance = 500,
        assets = Map("B" -> 100)
      )
    )
  }

  "balances" should "be updated correctly for two orders" in {
    updateBalances(balances, Seq(validBuyOrder, validSellOrder)) shouldBe Map(
      "C1" -> BalanceState(
        balance = 1091,
        assets = Map("A" -> 93)
      ),
      "C2" -> BalanceState(
        balance = 409,
        assets = Map(
          "A" -> 7,
          "B" -> 100
        )
      )
    )
  }
}
