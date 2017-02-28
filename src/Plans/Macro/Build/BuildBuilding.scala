package Plans.Macro.Build

import Development.TypeDescriber
import Plans.Allocation.{LockCurrency, LockCurrencyForUnit, LockUnits, LockUnitsExactly}
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionFinder, PositionSimpleBuilding}
import Strategies.UnitMatchers.{UnitMatchType, UnitMatcher}
import Strategies.UnitPreferences.{UnitPreferClose, UnitPreference}
import Types.Intents.Intent
import Types.UnitInfo.FriendlyUnitInfo
import Utilities.Property
import Utilities.Enrichment.EnrichPosition._
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
  
  var _builder:Option[FriendlyUnitInfo] = None
  var _building:Option[FriendlyUnitInfo] = None
  var _position:Option[TilePosition] = None
  var _lastOrderFrame = Integer.MIN_VALUE
    
  description.set(Some(TypeDescriber.describeUnitType(buildingType)))
  
  override def getChildren: Iterable[Plan] = { List(currencyPlan.get, builderPlan.get) }
  override def isComplete: Boolean = {
    _building.exists(building => building.complete || (building.alive && buildingType.getRace == Race.Protoss))
  }
  
  def startedBuilding:Boolean = {
    _building.isDefined
  }
  
  override def onFrame() {
    if (isComplete) {
      return
    }
  
    _building = With.units.ours
      .filter(unit => unit.unitType == buildingType)
      .filter(unit => _position.exists(position => position == unit.tilePosition))
      .headOption
  
    currencyPlan.get.isSpent = !_building.isEmpty
  
    currencyPlan.get.onFrame()
    if (currencyPlan.get.isComplete) {
      builderPlan.get.onFrame()
      if (builderPlan.get.isComplete) {
        _builder = builderPlan.get.units.headOption
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
    else if (builderPlan.get.isComplete) {
      //If the builder is available to us but we're not ready to build, let's just send it where it needs to go
      _position = positionFinder.get.find
      builderPlan.get.units.foreach(unit => With.commander.intend(
        unit,
        new Intent(position = Some(_position.map(_
          .toPosition
          .add(new Position(buildingType.width/2, buildingType.height/2)))
          .headOption
          .getOrElse(unit.position)))))
    }
  }
  
  def _orderToBuild(builder:FriendlyUnitInfo) {
    if (_lastOrderFrame < With.game.getFrameCount - 24) {
      _lastOrderFrame = With.game.getFrameCount
      
      if (_position.filter(p => With.game.canBuildHere(p, buildingType, builder.baseUnit)).isEmpty) {
        _position = positionFinder.get.find
      }
  
      if (_position.isEmpty) {
        With.logger.warn("Failed to place a " ++ buildingType.toString ++ " near " ++ _position.toString)
      }
      else {
        val positionExplored = With.game.isExplored(_position.get)
        
        // Can't order builds in fog of war
        if (positionExplored) {
          builder.baseUnit.build(buildingType, _position.get)
        } else {
          builder.baseUnit.move(_position.get.toPosition)
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
