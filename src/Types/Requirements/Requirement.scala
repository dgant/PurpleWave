package Types.Requirements

abstract class Requirement() {
  
  var buyer: Buyer = null
  var priorityMultiplier: PriorityMultiplier = null
  
  var isFulfilled:Boolean = false
  
  //Testing fulfillment by fulfilling can screw over plans down the priority chain by unassigning their resources and forcing them to reassign
  def fulfill()
  def abort()
  
  def priority():Integer = { -1 * priorityMultiplier.multiplier * buyer.priority }
}
