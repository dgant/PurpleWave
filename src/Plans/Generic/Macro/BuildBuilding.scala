package Plans.Generic.Macro

import Development.{Logger, TypeDescriber}
import Plans.Generic.Allocation.{LockCurrency, LockCurrencyForUnit, LockUnits, LockUnitsExactly}
import Plans.Plan
import Startup.With
import Strategies.PositionFinders.{PositionFinder, PositionSimpleBuilding}
import Strategies.UnitMatchers.{UnitMatchIncompleteBuilding, UnitMatchType, UnitMatcher}
import Strategies.UnitPreferences.{UnitPreferClose, UnitPreference}
import Traits.Property
import bwapi.{Position, Race, TilePosition, UnitType}

class BuildBuilding(val buildingType:UnitType) extends Plan {

  val me = this
  val positionFinder    = new Property[PositionFinder] (new PositionSimpleBuilding(buildingType))
  val builderMatcher    = new Property[UnitMatcher]    (new UnitMatchType(buildingType.whatBuilds.first))
  val builderPreference = new Property[UnitPreference] (new UnitPreferClose { positionFinder.inherit(me.positionFinder)})
  val buildingMatcher   = new Property[UnitMatcher]    (new UnitMatchIncompleteBuilding(buildingType))
  val currencyPlan      = new Property[LockCurrency]   (new LockCurrencyForUnit(buildingType))
  val builderPlan       = new Property[LockUnits]      (new LockUnitsExactly { unitMatcher.inherit(builderMatcher); unitPreference.inherit(builderPreference)})
  val buildingPlan      = new Property[LockUnits]      (new LockUnitsExactly { unitMatcher.inherit(buildingMatcher) })
  
  var _builder:Option[bwapi.Unit] = None
  var _building:Option[bwapi.Unit] = None
  var _position:Option[TilePosition] = None
  var _lastOrderFrame = Integer.MIN_VALUE
    
  description.set(Some(TypeDescriber.describeUnitType(buildingType)))
  
  override def getChildren: Iterable[Plan] = { List(currencyPlan.get, builderPlan.get, buildingPlan.get) }
  override def isComplete: Boolean = { _building.exists(_.isCompleted) }
  
  override def onFrame() {
    if (isComplete) {
      //It's important to quit so we release our resources
      return
    }
  
    currencyPlan.get.isSpent = !_building.isEmpty
  
    // Chill out if we have a Protoss building warping in
    if (_building.exists(_.exists) && buildingType.getRace == Race.Protoss) {
      return
    }
  
    currencyPlan.get.onFrame()
    if (currencyPlan.get.isComplete) {
      builderPlan.get.onFrame()
      if (builderPlan.get.isComplete) {
        _builder = builderPlan.get.units.headOption
        
        //We can probably simplify this
        if (currencyPlan.get.isComplete) {
          if (buildingType.getRace == Race.Terran) {
            _builder.foreach(b => _building = Option.apply(b.getBuildUnit))
  
            //Resume incomplete Terran buildings
            if (_building.isEmpty && buildingType.getRace == Race.Terran) {
              buildingPlan.get.onFrame()
              _building = buildingPlan.get.units.headOption
            }
          }

          // getBuildUnit() only works for Terran
          else {
            buildingPlan.get.onFrame()
            _building = buildingPlan.get.units.headOption
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
        _position = positionFinder.get.find
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
