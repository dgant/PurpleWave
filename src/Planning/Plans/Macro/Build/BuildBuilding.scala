package Planning.Plans.Macro.Build

import Debugging.Visualization.Rendering.DrawMap
import Micro.Intent.Intention
import Planning.Composition.PositionFinders.Buildings.PositionArbitraryBuilding
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import Planning.Composition.ResourceLocks.{LockArea, LockCurrencyForUnit, LockUnits}
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Lifecycle.With
import Utilities.EnrichPosition._
import bwapi.{Race, TilePosition}

class BuildBuilding(val buildingClass:UnitClass) extends Plan {
  
  val buildingPlacer = new PositionArbitraryBuilding(buildingClass)
  val areaLock = new LockArea
  val currencyLock = new LockCurrencyForUnit(buildingClass)
  val builderLock = new LockUnits {
    description.set("Get a builder")
    unitCounter.set(UnitCountOne)
    unitMatcher.set(new UnitMatchType(buildingClass.whatBuilds._1))
    unitPreference.set(new UnitPreferClose { positionFinder.set(buildingPlacer) })
  }
  private var builder:Option[FriendlyUnitInfo] = None
  private var building:Option[FriendlyUnitInfo] = None
  private var orderedTile:Option[TilePosition] = None
    
  description.set("Build a " + buildingClass)
  
  override def isComplete: Boolean = building.exists(b => b.alive && b.complete)
  
  def startedBuilding:Boolean = building.isDefined
  
  override def onFrame() {
    if (isComplete) return
    
    building = building.filter(_.alive)
    
    if (building.isEmpty && orderedTile.isDefined) {
      building = With.units.ours
        .filter(unit => unit.unitClass == buildingClass && unit.tileTopLeft == orderedTile.get)
        .headOption
    }
  
    currencyLock.isSpent = building.isDefined
    currencyLock.acquire(this)
    if ( ! currencyLock.satisfied) return
  
    if (building.isEmpty) {
      buildingPlacer.find.foreach(
        buildingTile => {
          areaLock.area = Some(buildingClass.tileArea.add(buildingTile))
          areaLock.acquire(this)
        })
    }
    
    if (areaLock.satisfied) {
        
      if (building.isEmpty || buildingClass.race == Race.Terran) {
        builderLock.acquire(this)
      }
      
      if (building.isEmpty && builderLock.satisfied) {
        orderedTile = Some(areaLock.area.get.startInclusive)
        With.executor.intend(
          new Intention(this, builderLock.units.head) {
            toBuild = Some(buildingClass)
            destination = orderedTile
          })
      }
    }
  }
  
  override def drawOverlay() {
    if (isComplete) return
    if ( ! orderedTile.isDefined) return
    if ( ! areaLock.satisfied) return
    DrawMap.box(
      buildingClass.tileArea.startInclusive.add(orderedTile.get).topLeftPixel,
      buildingClass.tileArea.endExclusive.add(orderedTile.get).topLeftPixel,
      DrawMap.playerColorDark(With.self))
    DrawMap.label(
      "Building a " + buildingClass.toString,
      orderedTile.get.toPosition,
      drawBackground = true,
      DrawMap.playerColorDark(With.self))
  }
}
