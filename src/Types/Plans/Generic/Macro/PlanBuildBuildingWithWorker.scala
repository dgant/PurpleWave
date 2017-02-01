package Types.Plans.Generic.Macro

import Startup.With
import Types.Plans.Generic.Allocation.{PlanAcquireCurrencyForUnit, PlanAcquireUnitsExactly}
import Types.Plans.Generic.Compound.PlanDelegateInSerial
import Types.PositionFinders.PositionFindBuildingSimple
import Types.Tactics.{Tactic, TacticBuildBuildingWithWorker}
import UnitMatchers.UnitMatchType
import bwapi.UnitType

class PlanBuildBuildingWithWorker(
  val builder:UnitType,
  val product:UnitType)
    extends PlanDelegateInSerial {
  
  val _currencyPlan = new PlanAcquireCurrencyForUnit(product)
  val _workerPlan = new PlanAcquireUnitsExactly(new UnitMatchType(builder), 1)
  _children = List(_currencyPlan, _workerPlan)
  
  val _positionFinder = new PositionFindBuildingSimple(product)
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
      _tactic = Some(new TacticBuildBuildingWithWorker(_workerPlan.units.head, product, _positionFinder))
    }
  
    _isFinished ||= _tactic.exists(_.isComplete)
    _tactic.filterNot(t => _isFinished).foreach(With.commander.queue(_))
  }
}
