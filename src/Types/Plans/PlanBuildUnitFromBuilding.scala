package Types.Plans

import Types.Requirements.{RequireAll, RequireCurrencyForUnit, RequireUnitsByQuantity}
import Types.Tactics.{Tactic, TacticBuildUnit}
import UnitMatching.Matcher.UnitMatchType
import bwapi.UnitType

class PlanBuildUnitFromBuilding(
  val builder:UnitType,
  val product:UnitType)
    extends Plan {
   
  val requireCurrency = new RequireCurrencyForUnit(product)
  val requireBuilder = new RequireUnitsByQuantity(1, new UnitMatchType(builder))
  override val requirements = new RequireAll(requireCurrency, requireBuilder)
  
  var _builder:Option[bwapi.Unit] = None
  var _product:Option[bwapi.Unit] = None
  var _tactic:Option[Tactic] = None
  
  override def execute(): Iterable[Tactic] = {
    if (_tactic == None) {
      if (requireCurrency.isAvailableNow) {
        _tactic = Some(new TacticBuildUnit(requireBuilder.units.head, product, None))
      }
    } else if (_tactic.get.isComplete) {
      flagComplete()
    }
    
    return _tactic.toIterable
  }
}
