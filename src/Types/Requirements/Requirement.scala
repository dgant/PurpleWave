package Types.Requirements

import Types.Contracts.{Buyer, PriorityMultiplier}

abstract class Requirement(
  val buyer: Buyer,
  val priorityMultiplier: PriorityMultiplier) {
  
  var isFulfilled:Boolean = false
  
  //Testing fulfillment by fulfilling can screw over plans down the priority chain by unassigning their resources and forcing them to reassign
  def fulfill()
  def abort()
  
  def priority():Integer = { -1 * priorityMultiplier.multiplier * buyer.priority }
}
