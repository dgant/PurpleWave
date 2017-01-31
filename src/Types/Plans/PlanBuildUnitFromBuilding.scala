package Types.Plans

import Types.Requirements.{RequireUnits, RequireUnitsByQuantity}
import Types.Tactics.{Tactic, TacticBuildUnit}
import UnitMatching.Matcher.UnitMatchType
import bwapi.UnitType

class PlanBuildUnitFromBuilding(
  val builder:UnitType,
  val product:UnitType)
    extends Plan {
   
  override val requirements:RequireUnits = new RequireUnitsByQuantity(1, new UnitMatchType(builder))
  
  var _builder:Option[bwapi.Unit] = None
  var _product:Option[bwapi.Unit] = None
  var _tactic:Option[Tactic] = None
  
  override def execute(): Iterable[Tactic] = {
    if (_tactic == None) {
      _tactic = Some(new TacticBuildUnit(requirements.units.head, product, None))
    }
    
    Iterable(_tactic.get)
  }
}
