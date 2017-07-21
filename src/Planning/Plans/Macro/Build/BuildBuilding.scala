package Planning.Plans.Macro.Build

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Scheduling.Project
import Mathematics.Points.Tile
import Micro.Intent.Intention
import Planning.Composition.ResourceLocks.{LockCurrencyForUnit, LockUnits}
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import bwapi.Race

class BuildBuilding(val buildingClass: UnitClass) extends Plan {
  
  val buildingDescriptor  = new Blueprint(this, Some(buildingClass))
  val currencyLock        = new LockCurrencyForUnit(buildingClass)
  
  private var desiredTile   : Option[Tile]              = None
  private var orderedTile   : Option[Tile]              = None
  private var building      : Option[FriendlyUnitInfo]  = None
  
  val builderMatcher = UnitMatchType(buildingClass.whatBuilds._1)
  val builderLock = new LockUnits {
    description.set("Get a builder")
    unitCounter.set(UnitCountOne)
    unitMatcher.set(builderMatcher)
    unitPreference.set(new UnitPreferClose)
  }
    
  description.set("Build a " + buildingClass)
  
  override def isComplete: Boolean = building.exists(_.aliveAndComplete)
  
  def startedBuilding: Boolean = building.isDefined
  
  var waitForBuilderToRecallUntil: Option[Int] = None
  
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
  
    desiredTile = acquireDesiredTile()
  
    if (desiredTile.isEmpty) {
      if (With.frame < With.configuration.maxFramesToTrustBuildRequest) {
        //Assume we'll find a build location eventually and reserve the currency anyway
        currencyLock.acquire(this)
      }
      return
    }
  
    currencyLock.framesAhead = (buildingClass.buildUnitsEnabling.map(enablingClass => Project.framesToUnits(enablingClass, 1)) :+ 0).max
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
    
    if (waitForBuilderToRecallUntil.isDefined) {
      if (With.frame < waitForBuilderToRecallUntil.get) {
        return
      }
      else {
        orderedTile = None
        waitForBuilderToRecallUntil = None
      }
    }
    
    if (builderLock.satisfied && building.isEmpty) {
      val builder = builderLock.units.head
      if (orderedTile.exists( ! desiredTile.contains(_))) {
        // The building placement has changed. This puts us at risk of building the same building twice.
        // We've already sent the builder out. We need to recall them if they haven't already started.
        // If we just issue another build order, latency may mean that they just started the building in the old location
        // Then, we wait to see if they have in fact started.
        //
        // Steps:
        // 1. Recall the builder
        // 2. Wait for the order to take effect
        waitForBuilderToRecallUntil = Some(With.frame + 24)
        builder.intend(new Intention(this) { toTravel = Some(desiredTile.get.pixelCenter); canAttack = false })
      } else {
        orderedTile = desiredTile
        builder.intend(
          new Intention(this) {
            toBuild     = if (currencyLock.isSatisfied) Some(buildingClass) else None
            toBuildTile = if (currencyLock.isSatisfied) orderedTile         else None
            toTravel    = Some(orderedTile.get.pixelCenter)
            canAttack   = false
          })
      }
    }
  }
  
  private def acquireDesiredTile(): Option[Tile] = {
    if (building.isDefined) {
      With.groundskeeper.flagFulfilled(buildingDescriptor)
      building.map(_.tileTopLeft)
    }
    else if (currencyLock.isSatisfied && currencyLock.expectedFrames < With.blackboard.maxFramesToSendAdvanceBuilder) {
      With.groundskeeper.demand(buildingDescriptor)
    }
    else {
      With.groundskeeper.require(buildingDescriptor)
    }
  }
  
  def needBuilder: Boolean = {
    if (building.isDefined && buildingClass.race != Race.Terran) {
      return false
    }
    if (desiredTile.isEmpty) {
      return false
    }
    if (currencyLock.expectedFrames > With.blackboard.maxFramesToSendAdvanceBuilder) {
      return false
    }
    val proposedBuilder = builderLock.inquire(this).flatMap(_.headOption)
    if (proposedBuilder.isEmpty) {
      return false
    }
    val travelFrames    = proposedBuilder.get.framesToTravel(desiredTile.get.pixelCenter) / With.configuration.assumedBuilderTravelSpeed
    val expectedFrames  = currencyLock.expectedFrames
    travelFrames + 24 >= expectedFrames
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
