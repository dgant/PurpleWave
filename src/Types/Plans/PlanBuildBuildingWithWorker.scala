package Types.Plans

import Processes.Architect
import Startup.With
import Types.Requirements.{RequireAll, RequireCurrencyForUnit, RequireUnitsByQuantity}
import Types.Tactics.Tactic
import Types.Tactics.Types.Tactics.TacticBuildBuildingWithWorker
import UnitMatching.Matcher.UnitMatchType
import bwapi.UnitType

import scala.collection.JavaConverters._

class PlanBuildBuildingWithWorker(
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
        val basePosition = With.game.self.getUnits.asScala
          .filter(_.getType.isBuilding)
          .sortBy( ! _.getType.isResourceDepot)
          .head
          .getTilePosition
        
        val position = Architect.placeBuilding(product, basePosition)
        _tactic = Some(new TacticBuildBuildingWithWorker(requireBuilder.units.head, product, position))
      }
    } else if (_tactic.get.isComplete) {
      _flagComplete()
    }
    
    return _tactic.toIterable
  }
  
}
