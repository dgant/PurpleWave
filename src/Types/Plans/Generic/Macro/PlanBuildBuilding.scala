package Types.Plans.Generic.Macro

import Development.Logger
import Startup.With
import Types.Plans.Generic.Allocation.{PlanAcquireCurrencyForUnit, PlanAcquireUnitsExactly}
import Types.Plans.Generic.Compound.PlanDelegateInSerial
import Types.PositionFinders.PositionFindBuildingSimple
import UnitMatchers.{UnitMatchType, UnitMatchTypeIncomplete}
import bwapi.UnitType

class PlanBuildBuilding(val buildingType:UnitType) extends PlanDelegateInSerial {
  
  val _positionFinder = new PositionFindBuildingSimple(buildingType)
  
  val _currencyPlan = new PlanAcquireCurrencyForUnit(buildingType)
  val _builderPlan = new PlanAcquireUnitsExactly(new UnitMatchType(buildingType.whatBuilds.first), 1)
  val _recyclePlan = new PlanAcquireUnitsExactly(new UnitMatchTypeIncomplete(buildingType), 1)
  
  _children = List(_currencyPlan, _builderPlan, _recyclePlan)
  
  var _builder:Option[bwapi.Unit] = None
  var _building:Option[bwapi.Unit] = None
  var _lastOrderFrame = Integer.MIN_VALUE
  
  override def isComplete(): Boolean = {
    _building.exists(_.isCompleted)
  }
  
  override def execute() {
    _currencyPlan.isSpent = ! _building.isEmpty
  
    if (isComplete) {
      abort()
      return
    }
  
    // Don't use the default serial execution.
    // We only want to execute the recycling plan if we don't have a builder
    //
    _currencyPlan.execute()
    if (_currencyPlan.isComplete) {
      _builderPlan.execute()
      if (_builderPlan.isComplete) {
        _builder = _builderPlan.units.headOption
        _builder.foreach(b => _building = Option.apply(b.getBuildUnit))
        if (_building.isEmpty) {
          _recyclePlan.execute()
          _building = _recyclePlan.units.headOption
        }
  
        if (_building.isEmpty) {
          _builder.foreach(_orderToBuild)
        }
        else {
          _builder.foreach(_.rightClick(_building.get))
        }
      }
    }
  }
  
  def _orderToBuild(builder:bwapi.Unit) {
    if (_lastOrderFrame < With.game.getFrameCount - 24) {
      _lastOrderFrame = With.game.getFrameCount
      val position = _positionFinder.find
      if (position.isEmpty) {
        Logger.warn("Failed to find a position to place a " ++ buildingType.toString)
      }
      else {
        _builder.get.build(buildingType, position.get)
      }
    }
  }
}
