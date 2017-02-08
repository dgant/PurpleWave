package Plans.Generic.Macro

import Development.{Logger, TypeDescriber}
import Plans.Generic.Allocation.{LockCurrency, LockCurrencyForUnit, LockUnits, LockUnitsExactly}
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionFinder, PositionSimpleBuilding}
import Strategies.UnitMatchers.{UnitMatchType, UnitMatcher}
import Strategies.UnitPreferences.{UnitPreferClose, UnitPreference}
import Types.Property
import bwapi.{Position, Race, TilePosition, UnitType}

class BuildBuilding(val buildingType:UnitType) extends Plan {

  val me = this
  val positionFinder    = new Property[PositionFinder] (new PositionSimpleBuilding(buildingType))
  val builderMatcher    = new Property[UnitMatcher]    (new UnitMatchType(buildingType.whatBuilds.first))
  val builderPreference = new Property[UnitPreference] (new UnitPreferClose { positionFinder.inherit(me.positionFinder)})
  val currencyPlan      = new Property[LockCurrency]   (new LockCurrencyForUnit(buildingType))
  val builderPlan       = new Property[LockUnits]      (new LockUnitsExactly {
    description.set(Some("Builder"));
    unitMatcher.inherit(builderMatcher);
    unitPreference.inherit(builderPreference)
  })
  
  var _builder:Option[bwapi.Unit] = None
  var _building:Option[bwapi.Unit] = None
  var _position:Option[TilePosition] = None
  var _lastOrderFrame = Integer.MIN_VALUE
    
  description.set(Some(TypeDescriber.describeUnitType(buildingType)))
  
  override def getChildren: Iterable[Plan] = { List(currencyPlan.get, builderPlan.get) }
  override def isComplete: Boolean = {
    _building.exists(building => building.isCompleted || (building.exists && buildingType.getRace == Race.Protoss))
  }
  
  def startedBuilding:Boolean = {
    _building.isDefined
  }
  
  override def onFrame() {
    if (isComplete) {
      return
    }
  
    _building = With.ourUnits
      .filter(unit => unit.getType == buildingType)
      .filter(unit => _position.exists(position => position == unit.getTilePosition))
      .headOption
  
    currencyPlan.get.isSpent = !_building.isEmpty
  
    currencyPlan.get.onFrame()
    if (currencyPlan.get.isComplete) {
      builderPlan.get.onFrame()
      if (builderPlan.get.isComplete) {
        _builder = builderPlan.get.units.headOption
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
  
  def _orderToBuild(builder:bwapi.Unit) {
    if (_lastOrderFrame < With.game.getFrameCount - 24) {
      _lastOrderFrame = With.game.getFrameCount
      
      if (_position.filter(p => With.game.canBuildHere(p, buildingType, builder)).isEmpty) {
        _position = positionFinder.get.find
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
