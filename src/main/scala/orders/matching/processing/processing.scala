package orders.matching

package object processing {
  type ClientId = String
  type AssetId = String
  type Orders = Map[AssetId, Vector[Order]]
  type Balances = Map[ClientId, BalanceState]
  type Currency = Int
}
