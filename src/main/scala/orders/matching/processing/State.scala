package orders.matching.processing

case class State(
  balances: Balances,
  activeOrders: Orders = Map()
)
