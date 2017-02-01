package Types.Plans.Generic.Macro

import Processes.Architect
import Startup.With
import Types.Plans.Generic.Allocation.{PlanAcquireCurrencyForUnit, PlanAcquireUnitsExactly}
import Types.Plans.Generic.Compound.PlanDelegateInSerial
import Types.Tactics.Tactic
import Types.Tactics.Types.Tactics.TacticBuildBuildingWithWorker
import UnitMatchers.UnitMatchType
import bwapi.UnitType

import scala.collection.JavaConverters._

class PlanBuildBuildingWithWorker(
  val builder:UnitType,
  val product:UnitType)
    extends PlanDelegateInSerial {
  
  val _currencyPlan = new PlanAcquireCurrencyForUnit(product)
  val _workerPlan = new PlanAcquireUnitsExactly(new UnitMatchType(builder), 1)
  _children = List(_currencyPlan, _workerPlan)
  
  var _tactic:Option[Tactic] = None
  var _isFinished = false
  
  override def isComplete(): Boolean = { _isFinished }
  
  override def execute() {
    if (_isFinished) {
      abort()
      return
    }
    
    super.execute()
    
    if (_tactic == None && _children.forall(_.isComplete)) {
      
      val basePosition = With.game.self.getUnits.asScala
        .filter(_.getType.isBuilding)
        .sortBy( ! _.getType.isResourceDepot)
        .head
        .getTilePosition
      
      val position = Architect.placeBuilding(product, basePosition)
      _tactic = Some(new TacticBuildBuildingWithWorker(_workerPlan.units.head, product, position))
    }
  
    _isFinished ||= _tactic.exists(_.isComplete)
    _tactic.filterNot(t => _isFinished).foreach(With.commander.queue(_))
  }
}
