package Plans.Generic.Macro

import Development.{Logger, TypeDescriber}
import Plans.Generic.Allocation.{PlanAcquireCurrencyForUnit, PlanAcquireUnitsExactly}
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionFinder, PositionSimpleBuilding}
import Strategies.UnitMatchers.{UnitMatchType, UnitMatchTypeAbandonedBuilding}
import Strategies.UnitPreferences.UnitPreferClose
import Traits.{TraitSettablePositionFinder, TraitSettableUnits}
import bwapi.{Position, Race, TilePosition, UnitType}

class BuildBuilding(val buildingType:UnitType)
  extends Plan
  with TraitSettableUnits
  with TraitSettablePositionFinder {
  
  var _builder:Option[bwapi.Unit] = None
  var _building:Option[bwapi.Unit] = None
  var _position:Option[TilePosition] = None
  var _lastOrderFrame = Integer.MIN_VALUE
  
  val _currencyPlan = new PlanAcquireCurrencyForUnit(buildingType)
  val _defaultBuilderPlan = new PlanAcquireUnitsExactly { setUnitMatcher(new UnitMatchType(buildingType.whatBuilds.first)) }
  val _ourNewBuilding = new PlanAcquireUnitsExactly {setUnitMatcher (new UnitMatchTypeAbandonedBuilding(buildingType)) }
  
  setUnits(_defaultBuilderPlan)
  override def setPositionFinder(value: PositionFinder) {
    super.setPositionFinder(value)
    _defaultBuilderPlan.setUnitPreference(new UnitPreferClose { setPositionFinder(value) })
    _ourNewBuilding.setUnitPreference(new UnitPreferClose { setPositionFinder(value) })
  }
  setPositionFinder(new PositionSimpleBuilding(buildingType))
  
  override def getDescription = {
    Some(super.getDescription.getOrElse(TypeDescriber.describeUnitType(buildingType)))
  }
  
  override def children(): Iterable[Plan] = {
    List(_currencyPlan, getUnits.get, _ourNewBuilding)
  }
  
  override def isComplete(): Boolean = {
    _building.exists(_.isCompleted)
  }
  
  override def onFrame() {
    if (isComplete) {
      //It's important to quit so we release our resources
      return
    }
  
    _currencyPlan.isSpent = !_building.isEmpty
  
  
    // Chill out if we have a Protoss building warping in
    if (_building.exists(_.exists) && buildingType.getRace == Race.Protoss) {
      return
    }
  
    _currencyPlan.onFrame()
    if (_currencyPlan.isComplete) {
      getUnits.foreach(_.onFrame())
      if (getUnits.exists(_.isComplete)) {
        _builder = getUnits.map(_.units.head).headOption
        
        //We can probably simplify this
        if (_currencyPlan.isComplete) {
          if (buildingType.getRace == Race.Terran) {
            _builder.foreach(b => _building = Option.apply(b.getBuildUnit))
  
            //Resume incomplete Terran buildings
            if (_building.isEmpty && buildingType.getRace == Race.Terran) {
              _ourNewBuilding.onFrame()
              _building = _ourNewBuilding.units.headOption
            }
          }

          // getBuildUnit() only works for Terran
          else {
            _ourNewBuilding.onFrame()
            _building = _ourNewBuilding.units.headOption
          }
  
          if (_building.isDefined) {
            if (buildingType.getRace == Race.Terran) {
              _builder.foreach(_.rightClick(_building.get))
            }
          }
          else {
            _builder.foreach(_orderToBuild)
          }
        }
      }
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
    if ( ! isComplete) {
    _position.foreach(position => {
      With.game.drawBoxMap(
        position.toPosition,
        new Position(
          32 * (position.getX + buildingType.tileWidth),
          32 * (position.getY + buildingType.tileHeight)),
        bwapi.Color.Green)
      With.game.drawTextMap(
        position.toPosition,
        "Building a " + buildingType.toString)})
    }
  }
}
