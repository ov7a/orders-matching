package orders.matching.io

import orders.matching.processing._

object BalancesParser {
  def parseClientBalance(line: String): Option[(ClientId, BalanceState)] = {
    val parts = line.split("\t")

    val clientId = parts(0)

    val balance = parts(1).toInt

    val assets =
      parts
        .drop(2)
        .zipWithIndex
        .map { case (value, index) => indexToAssetId(index) -> value.toInt }
        .toMap

    Some(clientId, BalanceState(balance, assets))
  }

  def parse(clientsFileName: String): Balances = {
    Parser
      .parseLines(clientsFileName, parseClientBalance)(_.toMap)
  }

  private def indexToAssetId(index: Int): AssetId = {
    ('A' + (index - 1)).toChar.toString
  }
}
