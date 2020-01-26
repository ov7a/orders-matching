package orders.matching.processing

sealed trait OrderProcessingResult

case class Success(
  state: State
) extends OrderProcessingResult

case object Failure extends OrderProcessingResult //TODO: encapsulate reason