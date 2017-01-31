package Types.Plans

import Types.Requirements.{RequireAll, RequireCurrencyForUnit, RequireUnitsByQuantity}
import Types.Tactics.{Tactic, TacticBuildUnitFromBuilding}
import UnitMatching.Matcher.UnitMatchType
import bwapi.UnitType

class PlanBuildUnitFromBuilding(
  val builder:UnitType,
  val product:UnitType)
    extends Plan {
   
  val requireCurrency = new RequireCurrencyForUnit(product)
  val requireBuilder = new RequireUnitsByQuantity(1, new UnitMatchType(builder))
  override val _requirements = new RequireAll(requireCurrency, requireBuilder)
  
  var _builder:Option[bwapi.Unit] = None
  var _product:Option[bwapi.Unit] = None
  var _tactic:Option[Tactic] = None
  
  override def execute(): Iterable[Tactic] = {
    super.execute()
    
    if (_tactic == None) {
      if (requireCurrency.isAvailableNow) {
        _tactic = Some(new TacticBuildUnitFromBuilding(requireBuilder.units.head, product, None))
      }
    } else if (_tactic.get.isComplete) {
      _flagComplete()
    }
    
    return _tactic.toIterable
  }
}
