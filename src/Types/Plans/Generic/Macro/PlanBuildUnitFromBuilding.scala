package Types.Plans.Generic.Macro

import Startup.With
import Types.Plans.Generic.Allocation.{PlanAcquireCurrencyForUnit, PlanAcquireUnitsExactly}
import Types.Plans.Generic.Compound.PlanDelegateInSerial
import Types.Tactics.{Tactic, TacticBuildUnitFromBuilding}
import UnitMatchers.UnitMatchType
import bwapi.UnitType

class PlanBuildUnitFromBuilding(
  val builder:UnitType,
  val product:UnitType)
    extends PlanDelegateInSerial {
  
  val _currencyPlan = new PlanAcquireCurrencyForUnit(product)
  val _builderPlan = new PlanAcquireUnitsExactly(new UnitMatchType(product.whatBuilds.first), 1)
  _children = List(_currencyPlan, _builderPlan)
  
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
      _tactic = Some(new TacticBuildUnitFromBuilding(
        _builderPlan.units.head,
        product))
    }
    
    _isFinished ||= _tactic.exists(_.isComplete)
    _tactic.filterNot(t => _isFinished).foreach(With.commander.queue(_))
  }
}
