package Planning.Plans.Macro.Build

import Debugging.Visualizations.Rendering.DrawMap
import Micro.Intent.Intention
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import Planning.Composition.ResourceLocks.{LockCurrencyForUnit, LockUnits}
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Mathematics.Points.Tile
import bwapi.Race

class BuildBuilding(val buildingClass: UnitClass) extends Plan {
  
  val buildingDescriptor  = new BuildingDescriptor(this, Some(buildingClass))
  val currencyLock        = new LockCurrencyForUnit(buildingClass)
  
  private var builder     : Option[FriendlyUnitInfo]  = None
  private var building    : Option[FriendlyUnitInfo]  = None
  private var orderedTile : Option[Tile]              = None
  
  val builderLock = new LockUnits {
    description.set("Get a builder")
    unitCounter.set(UnitCountOne)
    unitMatcher.set(UnitMatchType(buildingClass.whatBuilds._1))
    unitPreference.set(new UnitPreferClose)
  }
    
  description.set("Build a " + buildingClass)
  
  override def isComplete: Boolean = building.exists(_.aliveAndComplete)
  
  def startedBuilding:Boolean = building.isDefined
  
  override def onUpdate() {
    
    if (isComplete) return
    
    building = building
      .filter(_.alive)
      .orElse(
        orderedTile
          .map(tile => With.units.ours.find(unit =>
            unit.is(buildingClass) &&
            unit.tileTopLeft == tile))
          .getOrElse(None))
  
    val buildingTile = building
      .map(_.tileTopLeft)
      .orElse(With.groundskeeper.reserve(buildingDescriptor))
  
    currencyLock.isSpent = building.isDefined
    currencyLock.acquire(this)
    
    if (buildingTile.isDefined && currencyLock.satisfied) {
        
      if (building.isEmpty || buildingClass.race == Race.Terran) {
        builderLock.acquire(this)
      }
      
      if (building.isEmpty && builderLock.satisfied) {
        orderedTile = buildingTile
        With.executor.intend(
          new Intention(this, builderLock.units.head) {
            toBuild     = Some(buildingClass)
            toBuildTile = orderedTile
            toTravel    = Some(orderedTile.get.pixelCenter)
            canAttack   = false
          })
      }
    }
  }
  
  override def visualize() {
    if (isComplete) return
    if (orderedTile.isEmpty) return
    DrawMap.box(
      buildingClass.tileArea.startInclusive.add(orderedTile.get).topLeftPixel,
      buildingClass.tileArea.endExclusive.add(orderedTile.get).topLeftPixel,
      With.self.colorDark)
    DrawMap.label(
      "Building a " + buildingClass.toString,
      orderedTile.get.topLeftPixel,
      drawBackground = true,
      With.self.colorDark)
  }
}
