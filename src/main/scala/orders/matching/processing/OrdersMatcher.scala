package orders.matching.processing

import java.lang.Math.min

import scala.annotation.tailrec

object OrdersMatcher {
  def matchOrders(order: Order, newOrder: Order): OrderMatch = {
    newOrder match {
      case Order(order.clientId, _, order.assetId, _, _) =>
        SameClientMatch
      case Order(_, order.orderType.complement, order.assetId, _, order.amount) =>
        matchPrice(order, newOrder).map(FullMatch).getOrElse(NoMatch)
      case Order(_, order.orderType.complement, order.assetId, _, requestedAmount) =>
        matchPrice(order, newOrder).map(PartialMatch(_, min(order.amount, requestedAmount))).getOrElse(NoMatch)
      case Order(_, _, order.assetId, _, _) =>
        NoMatch
    }
  }

  @tailrec def matchPrice(order: Order, otherOrder: Order, firstCall: Boolean = true): Option[Currency] = {
    if (order.orderType == BuyOrderType && order.price >= otherOrder.price) {
      Some(otherOrder.price)
    } else if (firstCall) {
      matchPrice(otherOrder, order, firstCall = false)
    } else {
      None
    }
  }
}
