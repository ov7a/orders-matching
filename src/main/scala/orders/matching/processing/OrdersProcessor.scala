package orders.matching.processing

import scala.annotation.tailrec

object OrdersProcessor {
  //note: no transactions
  def process(
    state: State,
    newOrder: Order
  ): OrderProcessingResult = { //TODO: use lenses
    val assetId = newOrder.assetId

    state.activeOrders.getOrElse(assetId, Nil) match {
      case Nil =>
        val newState = state.copy(
          activeOrders = state.activeOrders.updated(assetId, List(newOrder))
        )
        Success(newState)

      case orderList =>
        val updatedOrderList = orderList.foldLeft(List[Order]()){(current, next) =>
          next match {
            case Order(client, newOrder.orderType.complement, assetId, price, amount) => current
            case _ => current.appended(next)
          }
        }
        val newState = state.copy(
          activeOrders = state.activeOrders.updated(assetId, updatedOrderList)
        )
        Success(newState)
    }
  }

  @tailrec private def matchOrderList(
    currentList: List[Order],
    order: Order,
    balances: Balances,
    updatedList: List[Order] = Nil
  ): (List[Order], Balances) = currentList match {

    case Nil => (updatedList :+ order, balances)

    case currentOrder :: tail =>
      matchOrders(currentOrder, order, balances) match {
        case SameClientMatch => {

        }
        case FullMatch => {

        }

        case PartialMatch =>{

        }

        case NoMatch => matchOrderList(tail, order, balances, updatedList :+ currentOrder)
      }
  }


  def matchOrders(order: Order, newOrder: Order, balances: Balances): OrderMatch = {

  }

  def processAll(
    state: State,
    orders: Iterator[Order]
  ): State = {
    orders.foldLeft(state) { (currentState, order) =>
      process(currentState, order) match {
        case Success(newState) => newState
        case Failure => throw new RuntimeException(s"Order $order processing failed with unknown reason")
      }
    }
  }
}
