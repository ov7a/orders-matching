package orders.matching.processing

sealed trait OrderProcessingResult

case class Success(
  state: State
) extends OrderProcessingResult

case object InvalidOrder extends OrderProcessingResult //TODO: encapsulate reason

case object Failure extends OrderProcessingResult //TODO: encapsulate reason