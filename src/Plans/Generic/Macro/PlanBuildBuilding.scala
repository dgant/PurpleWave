package Plans.Generic.Macro

import Development.{Logger, TypeDescriber}
import Plans.Generic.Allocation.{PlanAcquireCurrencyForUnit, PlanAcquireUnitsExactly}
import Plans.Plan
import Startup.With
import Types.PositionFinders.{PositionFinder, PositionSimpleBuilding}
import Types.UnitMatchers.{UnitMatchType, UnitMatchTypeAbandonedBuilding}
import bwapi.{Race, TilePosition, UnitType}

class PlanBuildBuilding(val buildingType:UnitType) extends Plan {
  
  var _positionFinder:PositionFinder = new PositionSimpleBuilding(buildingType)
  
  val _currencyPlan = new PlanAcquireCurrencyForUnit(buildingType)
  val _builderPlan = new PlanAcquireUnitsExactly(new UnitMatchType(buildingType.whatBuilds.first), 1)
  val _recyclePlan = new PlanAcquireUnitsExactly(new UnitMatchTypeAbandonedBuilding(buildingType), 1)
  
  _children = List(_currencyPlan, _builderPlan, _recyclePlan)
  
  var _builder:Option[bwapi.Unit] = None
  var _building:Option[bwapi.Unit] = None
  var _position:Option[TilePosition] = None
  var _lastOrderFrame = Integer.MIN_VALUE
  
  override def describe(): Option[String] = {
    Some(TypeDescriber.describeUnitType(buildingType))
  }
  
  override def isComplete(): Boolean = {
    _building.exists(_.isCompleted)
  }
  
  override def execute() {
    _currencyPlan.isSpent = ! _building.isEmpty
  
    if (isComplete) {
      abort()
      return
    }
    
    // Chill out if we have a Protoss building warping in
    if (_building.exists(_.exists) && buildingType.getRace == Race.Protoss) {
      _builderPlan.abort
      return
    }
    
    _currencyPlan.execute()
    if (_currencyPlan.isComplete) {
      _builderPlan.execute()
      if (_builderPlan.isComplete) {
        _builder = _builderPlan.units.headOption
        
        if (buildingType.getRace == Race.Terran) {
          _builder.foreach(b => _building = Option.apply(b.getBuildUnit))
  
          //Resume incomplete Terran buildings
          if (_building.isEmpty && buildingType.getRace == Race.Terran) {
            _recyclePlan.execute()
            _building = _recyclePlan.units.headOption
          }
        }
        // getBuildUnit() only works for Terran
        else {
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
      
      if (_position.filter(p => With.game.canBuildHere(p, buildingType, builder)).isEmpty) {
        _position = _positionFinder.find
      }
  
      if (_position.isEmpty) {
        Logger.warn("Failed to place a " ++ buildingType.toString ++ " near " ++ _position.toString)
      }
      else {
        val positionExplored = With.game.isExplored(_position.get)
        
        // Can't order builds in fog of war
        if (positionExplored) {
          builder.build(buildingType, _position.get)
        } else {
          builder.move(_position.get.toPosition)
        }
      }
    }
  }
}
