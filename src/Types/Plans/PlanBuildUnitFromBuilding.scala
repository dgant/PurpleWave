package Types.Plans

import Types.Requirements.{PriorityMinimum, RequireUnits}
import Types.Tactics.{Tactic, TacticBuildUnit}
import UnitMatching.Matcher.UnitMatchType
import bwapi.UnitType

class PlanBuildUnitFromBuilding(
  val builder:UnitType,
  val product:UnitType)
    extends Plan {
   
  override val requirementsMinimal:RequireUnits = new RequireUnits(this, PriorityMinimum, new UnitMatchType(builder), 1)
  
  var _builder:Option[bwapi.Unit] = None
  var _product:Option[bwapi.Unit] = None
  
  var _tactic:Option[Tactic] = None
  
  override def execute(): Iterable[Tactic] = {
    if (_tactic == None) {
      _tactic = Some(new TacticBuildUnit(requirementsMinimal.units.head, product, None))
    }
    
    Iterable(_tactic.get)
  }
}
