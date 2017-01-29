package Types.Plans

import Types.Contracts.Buyer
import Types.Requirements.{RequireNothing, Requirement}
import Types.Tactics.Tactic

abstract class Plan (
  var requirementsMinimal:Requirement = new RequireNothing,
  var requirementsOptimal:Requirement = new RequireNothing,
  var requirementsOptional:Requirement = new RequireNothing)
    extends Buyer {
  
  def update() = {}
  def children():Iterable[Plan] = { return List.empty }
  def execute():Iterable[Tactic] = { return List.empty }
}