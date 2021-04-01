package orders.matching.io

import orders.matching.processing.{BuyOrderType, Order, SellOrderType}

object OrdersParser {
  def parseOrder(line: String): Option[Order] = {
    val parts = line.split(Parser.separator)
    val clientId = parts(0)
    val orderTypeRaw = parts(1)
    val orderType = orderTypeRaw match {
      case "s" => SellOrderType
      case "b" => BuyOrderType
      case _ => throw new IllegalArgumentException(s"Unknown order type: $orderTypeRaw")
    }
    val assetId = parts(2)
    val price = parts(3).toInt
    val amount = parts(4).toInt

    Some(Order(
      clientId = clientId,
      orderType = orderType,
      assetId = assetId,
      price = price,
      amount = amount
    ))
  }

  def parse[R](inputId: String): (Iterator[Order] => R) => R = {
    Parser
      .parseLines(inputId, parseOrder)
  }
}
