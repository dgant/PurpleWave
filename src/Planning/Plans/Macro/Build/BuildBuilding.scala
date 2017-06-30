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
  
  private var desiredTile   : Option[Tile]              = None
  private var orderedTile   : Option[Tile]              = None
  private var builder       : Option[FriendlyUnitInfo]  = None
  private var building      : Option[FriendlyUnitInfo]  = None
  
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
    
    if (isComplete) {
      With.groundskeeper.flagFulfilled(buildingDescriptor)
      return
    }
    
    building = building
      .filter(_.alive)
      .orElse(
        orderedTile
          .map(tile => With.units.ours.find(unit =>
            unit.is(buildingClass) &&
            unit.tileTopLeft == tile))
          .getOrElse(None))
  
    desiredTile =
      if (building.isDefined) {
        With.groundskeeper.flagFulfilled(buildingDescriptor)
        building.map(_.tileTopLeft)
      }
      else {
        With.groundskeeper.require(buildingDescriptor)
      }
  
    if (desiredTile.isEmpty) {
      if (With.frame < With.configuration.maxFramesToTrustBuildRequest) {
        //Assume we'll find a build location eventually and reserve the currency anyway
        currencyLock.acquire(this)
      }
      return
    }
    
    currencyLock.isSpent = building.isDefined
    currencyLock.acquire(this)
    
    if ( ! needBuilder) {
      return
    }
  
    // When building placement changes we want a builder closer to the new placement
    if (orderedTile.isDefined && orderedTile != desiredTile) {
      builderLock.release()
    }
    builderLock.unitPreference.set(UnitPreferClose(desiredTile.get.pixelCenter))
    builderLock.acquire(this)
    
    if (builderLock.satisfied && building.isEmpty) {
      orderedTile = desiredTile
      With.executor.intend(
        new Intention(this, builderLock.units.head) {
          toBuild     = if (currencyLock.isSatisfied) Some(buildingClass) else None
          toBuildTile = if (currencyLock.isSatisfied) orderedTile         else None
          toTravel    = Some(orderedTile.get.pixelCenter)
          canAttack   = false
        })
    }
  }
  
  def needBuilder: Boolean = {
    if (building.isDefined && buildingClass.race != Race.Terran) {
      return false
    }
    if (desiredTile.isEmpty) {
      return false
    }
    val proposedBuilders = builderLock.inquire(this)
    proposedBuilders.exists(
      _.exists(someBuilder =>
        Math.min(With.configuration.maxFramesToSendAdvanceBuilder, someBuilder.framesToTravel(desiredTile.get.pixelCenter)) >=
        currencyLock.expectedFrames))
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
