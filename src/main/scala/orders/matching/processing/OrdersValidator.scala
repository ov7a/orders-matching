package orders.matching.processing

object OrdersValidator {
  def validate(
    order: Order,
    balances: Balances
  ): Boolean = {
    order match {
      case Order(_, _, _, price, amount) if price <= 0 || amount <= 0 =>
        false
      case Order(clientId, BuyOrderType, _, price, amount) if balances(clientId).balance < price * amount =>
        false
      case Order(clientId, SellOrderType, assetId, _, amount) if balances(clientId).assets.getOrElse(assetId, 0) < amount =>
        false
      case _ =>
        true
    }
  }
}
