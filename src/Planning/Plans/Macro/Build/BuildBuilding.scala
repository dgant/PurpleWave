package Planning.Plans.Macro.Build

import Debugging.TypeDescriber
import Debugging.Visualization.DrawMap
import Micro.Behaviors.DefaultBehavior
import Micro.Intentions.Intention
import Planning.Composition.PositionFinders.PositionSimpleBuilding
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import Planning.Plans.Allocation.{LockCurrencyForUnit, LockUnits}
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._
import bwapi.{Position, Race, TilePosition}

class BuildBuilding(val buildingType:UnitClass) extends Plan {
  
  val buildingPlacer = new PositionSimpleBuilding(buildingType)
  val currencyPlan = new LockCurrencyForUnit(buildingType)
  val builderPlan = new LockUnits {
    description.set("Get a builder")
    unitCounter.set(UnitCountOne)
    unitMatcher.set(new UnitMatchType(buildingType.whatBuilds._1))
    unitPreference.set(new UnitPreferClose { positionFinder.set(buildingPlacer)})
  }
  
  private var builder:Option[FriendlyUnitInfo] = None
  private var building:Option[FriendlyUnitInfo] = None
  private var position:Option[TilePosition] = None
  private var lastOrderFrame = Integer.MIN_VALUE
    
  description.set("Build a " + TypeDescriber.unit(buildingType))
  
  override def getChildren: Iterable[Plan] = List(currencyPlan, builderPlan)
  override def isComplete: Boolean =
    building.exists(building => building.complete || (building.alive && buildingType.getRace == Race.Protoss))
  
  def startedBuilding:Boolean = building.isDefined
  
  override def onFrame() {
    if (isComplete) return
  
    building = With.units.ours
      .filter(unit => unit.utype == buildingType)
      .filter(unit => position.exists(_ == unit.tileTopLeft))
      .headOption
  
    currencyPlan.isSpent = building.isDefined
  
    currencyPlan.onFrame()
    if (currencyPlan.isComplete) {
      builderPlan.onFrame()
      if (builderPlan.isComplete) {
        builder = builderPlan.units.headOption
        if (building.isDefined) {
          if (buildingType.getRace == Race.Terran) {
            builder.foreach(_.baseUnit.rightClick(building.get.baseUnit))
          }
        }
        else {
          builder.foreach(orderToBuild)
        }
      }
    }
    else if (builderPlan.isComplete) {
      //If the builder is available to us but we're not ready to build, let's just send it where it needs to go
      position = buildingPlacer.find
      builderPlan.units.foreach(unit => With.executor.intend(
        new Intention(
          this,
          unit,
          DefaultBehavior,
          position.map(_.add(1, 1)).headOption.getOrElse(unit.pixel.toTilePosition))))
    }
  }
  
  private def orderToBuild(builder:FriendlyUnitInfo) {
    if (lastOrderFrame < With.frame - 24) {
      lastOrderFrame = With.frame
      position = buildingPlacer.find
      if (position.nonEmpty)  {
        // This avoids trying to build in fog of war
        if (builder.distance(position.get) < 32 * 6) {
          builder.baseUnit.build(buildingType.baseType, position.get)
        } else {
          With.executor.intend(new Intention(this, builder, DefaultBehavior, position.get))
        }
      }
    }
  }
  
  override def drawOverlay() {
    if (isComplete) return
    position.foreach(position => {
      DrawMap.box(
        position.toPosition,
        new Position(
          32 * (position.getX + buildingType.tileWidth),
          32 * (position.getY + buildingType.tileHeight)),
        DrawMap.playerColor(With.self))
      DrawMap.label(
        "Building a " + buildingType.toString,
        position.toPosition,
        drawBackground = true,
        DrawMap.playerColor(With.self))
    })
  }
}
