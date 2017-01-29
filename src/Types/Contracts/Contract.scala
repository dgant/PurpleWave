package Types.Contracts

abstract class Contract(
  val buyer:Buyer,
  val priority:PriorityMultiplier) {
  
  var requirementsMet = false
  
  def calculatePriority():Integer = {
    priority.multiplier * buyer.priority
  }
}
