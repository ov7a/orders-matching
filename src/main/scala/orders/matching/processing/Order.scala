package orders.matching.processing

case class Order(
  clientId: ClientId,
  orderType: OrderType,
  assetId: AssetId,
  price: Currency,
  amount: Int
)

sealed trait OrderType{
  val complement: OrderType
}

case object BuyOrderType extends OrderType{
  override val complement: OrderType = SellOrderType
}
case object SellOrderType extends OrderType{
  override val complement: OrderType = BuyOrderType
}
