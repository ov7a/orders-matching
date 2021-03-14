package orders.matching.processing

import orders.matching.processing.BalancesUpdater.updateBalances
import orders.matching.processing.OrdersMatcher.matchOrders
import orders.matching.processing.OrdersValidator.validate

import scala.annotation.tailrec

object OrdersProcessor {
  //note: no transactions
  def process(
    state: State,
    newOrder: Order
  ): OrderProcessingResult = { //TODO: use lenses
    if (!validate(newOrder, state.balances)) {
      InvalidOrder
    } else {
      val assetId = newOrder.assetId
      state.activeOrders.getOrElse(assetId, Nil) match {
        case Nil =>
          val newState = state.copy(
            activeOrders = state.activeOrders.updated(assetId, List(newOrder))
          )
          Success(newState)

        case orderList =>
          val (updatedOrderList, updatedBalances) = matchOrderList(orderList, newOrder, state.balances)
          val newState = state.copy(
            balances = updatedBalances,
            activeOrders = state.activeOrders.updated(assetId, updatedOrderList)
          )
          Success(newState)
      }
    }
  }

  @tailrec private def matchOrderList(
    currentList: List[Order],
    order: Order,
    balances: Balances,
    updatedList: List[Order] = Nil
  ): (List[Order], Balances) = currentList match {

    case Nil => (updatedList :+ order, balances)

    case currentOrder :: tail if !validate(currentOrder, balances) =>
      matchOrderList(tail, order, balances, updatedList)

    case currentOrder :: tail =>
      matchOrders(currentOrder, order) match {
        case FullMatch(price) =>
          val updatedBalances = updateBalances(
            balances,
            Seq(currentOrder.copy(price = price), order.copy(price = price))
          )

          (updatedList ++ tail, updatedBalances)
        case PartialMatch(price, amount) =>
          val updatedBalances = updateBalances(
            balances,
            Seq(currentOrder.copy(price = price, amount = amount), order.copy(price = price, amount = amount))
          )
          val leftOver = if (order.amount > amount) {
            order.copy(amount = order.amount - amount)
          } else if (currentOrder.amount > amount) {
            currentOrder.copy(amount = currentOrder.amount - amount)
          } else {
            throw new RuntimeException("Partial matching is not fully implemented")
          }

          ((updatedList :+ leftOver) ++ tail, updatedBalances)
        case SameClientMatch | NoMatch =>
          matchOrderList(tail, order, balances, updatedList :+ currentOrder)
      }
  }

  def processAll(
    state: State,
    orders: Iterator[Order]
  ): State = {
    orders.foldLeft(state) { (currentState, order) =>
      process(currentState, order) match {
        case Success(newState) => newState
        case InvalidOrder => currentState
        case Failure => throw new RuntimeException(s"Order $order processing failed with unknown reason")
      }
    }
  }
}
