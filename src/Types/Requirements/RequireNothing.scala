package Types.Requirements

class RequireNothing(
  buyer:Buyer,
  priorityMultiplier: PriorityMultiplier)
  extends Requirement(
    buyer,
    priorityMultiplier) {
  
  //Testing fulfillment by fulfilling can screw over plans down the priority chain by unassigning their resources and forcing them to reassign
  override def fulfill() { isFulfilled = true}
  override def abort() { isFulfilled = false }
}
