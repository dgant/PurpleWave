package Types.Plans

import Types.Requirements._
import Types.Tactics.Tactic

abstract class Plan extends Buyer {
  val requirementsMinimal:Requirement = new RequireNothing(this, PriorityMinimum)
  val requirementsOptimal:Requirement = new RequireNothing(this, PriorityOptimal)
  val requirementsOptional:Requirement = new RequireNothing(this, PriorityOptional)
  
  def update() = {}
  def children():Iterable[Plan] = { return List.empty }
  def execute():Iterable[Tactic] = { return List.empty }
  
  def active:Boolean = { requirementsMinimal.isFulfilled }
  def abort() {
    requirementsMinimal.abort()
    requirementsOptimal.abort()
    requirementsOptional.abort()
  }
}