package Plans.Macro.Build

import Development.TypeDescriber
import Global.Combat.Commands.Approach
import Plans.Allocation.{LockCurrencyForUnit, LockUnits}
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.PositionSimpleBuilding
import Strategies.UnitCounters.UnitCountOne
import Strategies.UnitMatchers.UnitMatchType
import Strategies.UnitPreferences.UnitPreferClose
import Types.Intents.Intention
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Enrichment.EnrichPosition._
import bwapi.{Position, Race, TilePosition, UnitType}

class BuildBuilding(val buildingType:UnitType) extends Plan {
  
  val buildingPlacer = new PositionSimpleBuilding(buildingType)
  val currencyPlan = new LockCurrencyForUnit(buildingType)
  val builderPlan = new LockUnits {
    description.set(Some("Get a builder"));
    unitCounter.set(UnitCountOne)
    unitMatcher.set(new UnitMatchType(buildingType.whatBuilds.first))
    unitPreference.set(new UnitPreferClose { positionFinder.set(buildingPlacer)})
  }
  
  var _builder:Option[FriendlyUnitInfo] = None
  var _building:Option[FriendlyUnitInfo] = None
  var _position:Option[TilePosition] = None
  var _lastOrderFrame = Integer.MIN_VALUE
    
  description.set(Some("Build a " + TypeDescriber.describeUnitType(buildingType)))
  
  override def getChildren: Iterable[Plan] = List(currencyPlan, builderPlan)
  override def isComplete: Boolean =
    _building.exists(building => building.complete || (building.alive && buildingType.getRace == Race.Protoss))
  
  def startedBuilding:Boolean = _building.isDefined
  
  override def onFrame() {
    if (isComplete) { return }
  
    _building = With.units.ours
      .filter(unit => unit.utype == buildingType)
      .filter(unit => _position.exists(_ == unit.tileTopLeft))
      .headOption
  
    currencyPlan.isSpent = !_building.isEmpty
  
    currencyPlan.onFrame()
    if (currencyPlan.isComplete) {
      builderPlan.onFrame()
      if (builderPlan.isComplete) {
        _builder = builderPlan.units.headOption
        if (_building.isDefined) {
          if (buildingType.getRace == Race.Terran) {
            _builder.foreach(_.baseUnit.rightClick(_building.get.baseUnit))
          }
        }
        else {
          _builder.foreach(_orderToBuild)
        }
      }
    }
    else if (builderPlan.isComplete) {
      //If the builder is available to us but we're not ready to build, let's just send it where it needs to go
      _position = buildingPlacer.find
      builderPlan.units.foreach(unit => With.commander.intend(
        new Intention(
          this,
          unit,
          Approach,
          _position.map(_.add(1, 1)).headOption.getOrElse(unit.position.toTilePosition))))
    }
  }
  
  def _orderToBuild(builder:FriendlyUnitInfo) {
    if (_lastOrderFrame < With.game.getFrameCount - 24) {
      _lastOrderFrame = With.game.getFrameCount
      
      if ( ! _position.exists(p => With.game.canBuildHere(p, buildingType, builder.baseUnit))) {
        _position = buildingPlacer.find
      }
  
      if (_position.isEmpty) {
        With.logger.warn("Failed to place a " ++ buildingType.toString ++ " near " ++ _position.toString)
      }
      else {
        // This avoids trying to build in fog of war
        if (builder.distance(_position.get) < 32 * 6) {
          builder.baseUnit.build(buildingType, _position.get)
        } else {
          With.commander.intend(new Intention(this, builder, Approach, _position.get))
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
