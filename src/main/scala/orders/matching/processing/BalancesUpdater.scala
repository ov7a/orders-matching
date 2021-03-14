package orders.matching.processing

object BalancesUpdater {
  def updateBalances(balances: Balances, orders: Seq[Order]): Balances = {
    orders.foldLeft(balances) { (oldBalances, order) =>
      val oldBalanceState = oldBalances(order.clientId)
      val newBalanceState = order.orderType match {
        case BuyOrderType => oldBalanceState.copy(
          balance = oldBalanceState.balance - order.price * order.amount,
          assets = oldBalanceState.assets.updated(order.assetId, oldBalanceState.assets.getOrElse(order.assetId, 0) + order.amount)
        )
        case SellOrderType => oldBalanceState.copy(
          balance = oldBalanceState.balance + order.price * order.amount,
          assets = oldBalanceState.assets.updated(order.assetId, oldBalanceState.assets(order.assetId) - order.amount)
        )
      }
      oldBalances.updated(order.clientId, newBalanceState)
    }
  }
}
