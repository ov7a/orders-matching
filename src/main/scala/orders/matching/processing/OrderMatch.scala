package orders.matching.processing

sealed trait OrderMatch

case object SameClientMatch extends OrderMatch

case class FullMatch(price: Currency) extends OrderMatch

case class PartialMatch(price: Currency, amount: Int) extends OrderMatch

case object NoMatch extends OrderMatch