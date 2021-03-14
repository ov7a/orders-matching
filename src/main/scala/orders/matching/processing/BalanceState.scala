package orders.matching.processing

case class BalanceState(
  balance: Currency,
  assets: Map[AssetId, Int]
)
