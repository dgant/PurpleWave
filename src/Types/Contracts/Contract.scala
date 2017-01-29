package Types.Contracts

abstract class Contract(
  val buyer:Buyer,
  val priority:PriorityMultiplier) {
  
  var active = true
  
  def priority():Integer = {
    priority * buyer.priority
  }
}
