package orders.matching

package object processing {
  type ClientId = String
  type AssetId = String
  type Orders = Map[AssetId, List[Order]]
  type Balances = Map[ClientId, BalanceState]
}
