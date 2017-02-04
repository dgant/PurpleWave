package Plans.Generic.Macro

import Development.{Logger, TypeDescriber}
import Plans.Generic.Allocation.{PlanAcquireCurrencyForUnit, PlanAcquireUnitsExactly}
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionFinder, PositionSimpleBuilding}
import Strategies.UnitMatchers.{UnitMatchType, UnitMatchTypeAbandonedBuilding}
import Strategies.UnitPreferences.UnitPreferClose
import Traits.TraitSettablePositionFinder
import bwapi.{Position, Race, TilePosition, UnitType}

class BuildBuilding(val buildingType:UnitType)
  extends Plan
  with TraitSettablePositionFinder {
  
  var monopolizeWorker:Boolean = false
  
  val _currencyPlan = new PlanAcquireCurrencyForUnit(buildingType)
  val _builderPlan = new PlanAcquireUnitsExactly { setUnitMatcher(new UnitMatchType(buildingType.whatBuilds.first)) }
  val _recyclePlan = new PlanAcquireUnitsExactly {setUnitMatcher (new UnitMatchTypeAbandonedBuilding(buildingType)) }
  
  override def setPositionFinder(value: PositionFinder) {
    super.setPositionFinder(value)
    _builderPlan.setUnitPreference(new UnitPreferClose { setPositionFinder(value) })
    _recyclePlan.setUnitPreference(new UnitPreferClose { setPositionFinder(value) })
  }
  setPositionFinder(new PositionSimpleBuilding(buildingType))
  
  var _builder:Option[bwapi.Unit] = None
  var _building:Option[bwapi.Unit] = None
  var _position:Option[TilePosition] = None
  var _lastOrderFrame = Integer.MIN_VALUE
  
  override def describe(): Option[String] = {
    Some(TypeDescriber.describeUnitType(buildingType))
  }
  
  override def children(): Iterable[Plan] = {
    List(_currencyPlan, _builderPlan, _recyclePlan)
  }
  
  override def isComplete(): Boolean = {
    _building.exists(_.isCompleted)
  }
  
  override def onFrame() {
    if (isComplete) {
      //It's important to quit so we release our resources
      return
    }
    
    _currencyPlan.isSpent = ! _building.isEmpty


    // Chill out if we have a Protoss building warping in
    if (_building.exists(_.exists) && buildingType.getRace == Race.Protoss) {
      //Don't let that worker go!
      if (monopolizeWorker) {
        _builderPlan.onFrame()
      }
      return
    }
    
    _currencyPlan.onFrame()
    if (_currencyPlan.isComplete || monopolizeWorker) {
      _builderPlan.onFrame()
      if (_builderPlan.isComplete) {
        _builder = _builderPlan.units.headOption
        
        if ( _currencyPlan.isComplete) {
          _construct()
        }
        else {
          _builder.foreach(builder =>
            getPositionFinder.find.foreach(position =>
              builder.move(position.toPosition)))
        }
      }
    }
  }
  
  def _construct() {
    if (buildingType.getRace == Race.Terran) {
      _builder.foreach(b => _building = Option.apply(b.getBuildUnit))
    
      //Resume incomplete Terran buildings
      if (_building.isEmpty && buildingType.getRace == Race.Terran) {
        _recyclePlan.onFrame()
        _building = _recyclePlan.units.headOption
      }
    }
      
    // getBuildUnit() only works for Terran
    else {
      _recyclePlan.onFrame()
      _building = _recyclePlan.units.headOption
    }
  
    if (_building.isEmpty) {
      _builder.foreach(_orderToBuild)
    }
    else {
      _builder.foreach(_.rightClick(_building.get))
    }
  }
  
  def _orderToBuild(builder:bwapi.Unit) {
    if (_lastOrderFrame < With.game.getFrameCount - 24) {
      _lastOrderFrame = With.game.getFrameCount
      
      if (_position.filter(p => With.game.canBuildHere(p, buildingType, builder)).isEmpty) {
        With.architect.setBuilder(builder)
        _position = getPositionFinder.find
        With.architect.clearBuilder()
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
  
  override def drawOverlay() = {
    _position.foreach(position => {
      With.game.drawBoxMap(
        position.toPosition,
        new Position(
          position.toPosition.getX + buildingType.width,
          position.toPosition.getY + buildingType.height),
        bwapi.Color.Green)
      With.game.drawTextMap(
        position.toPosition,
        "Building a " + buildingType.toString)})
  }
}
