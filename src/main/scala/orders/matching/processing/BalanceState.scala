package orders.matching.processing

case class BalanceState(
  balance: Int,
  assets: Map[AssetId, Int]
)
