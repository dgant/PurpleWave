package Planning.Plans.Macro.Build

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Buildables.{Buildable, BuildableUnit}
import Mathematics.Points.Tile
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyForUnit, LockUnits}
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers.{UnitMatchAnd, UnitMatchCustom, UnitMatchSpecific}
import Planning.UnitPreferences.UnitPreferCloseAndNotMining
import ProxyBwapi.Races.Neutral
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class BuildBuilding(val buildingClass: UnitClass) extends ProductionPlan {

  override def producerCurrencyLocks: Seq[LockCurrency] = Seq(currencyLock)
  override def producerUnitLocks: Seq[LockUnits] = Seq(builderLock)
  override def producerInProgress: Boolean = building.isDefined
  override def buildable: Buildable = BuildableUnit(buildingClass)

  val buildingDescriptor  = new Blueprint(this, Some(buildingClass))
  val currencyLock        = new LockCurrencyForUnit(buildingClass)
  
  private var desiredTile   : Option[Tile]              = None
  private var orderedTile   : Option[Tile]              = None
  private var building      : Option[FriendlyUnitInfo]  = None
  
  val builderMatcher = buildingClass.whatBuilds._1
  val builderLock = new LockUnits {
    description.set("Get a builder")
    unitCounter.set(UnitCountOne)
    unitMatcher.set(builderMatcher)
    interruptable.set(false)
  }
    
  description.set("Build a " + buildingClass)
  
  override def isComplete: Boolean = building.exists(_.complete)
  
  def startedBuilding: Boolean = building.isDefined
  
  var waitForBuilderToRecallUntil: Option[Int] = None
  
  override def onUpdate() {

    building = building.filter(b => b.alive && ! b.is(Neutral.Geyser))
    
    if (building.exists(b => ! b.unitClass.isTerran)) {
      builderLock.release()
      With.groundskeeper.flagFulfilled(buildingDescriptor, building.get)
      return
    }
    
    building = building
      .orElse(
        if (buildingClass.isTerran)
          With.units.ours.find(scaffolding =>
            ! scaffolding.complete
            && scaffolding.is(buildingClass)
            && ! scaffolding.buildUnit.exists(_.friendly.forall(builder =>
              // This probably doesn't handle addon construction correctly
              builder.agent.toFinish.contains(scaffolding)
              || builder.agent.toBuildTile.contains(scaffolding.tileTopLeft)
            )))
        else None
      )
      .orElse(
        orderedTile.flatMap(tile => With.units.ours.find(unit =>
          unit.is(buildingClass) &&
            unit.tileTopLeft == tile)))
      .filter(b =>
        b.isOurs
        && b.alive
        && b.buildUnit.forall(_.friendly.forall(_.agent.lastClient.contains(this)))) //Don't jack another (Terran) building
    
    desiredTile = acquireDesiredTile()
  
    if (desiredTile.isEmpty) {
      if (With.frame < With.configuration.maxFramesToTrustBuildRequest) {
        //Assume we'll find a build location eventually and reserve the currency anyway
        currencyLock.acquire(this)
      }
      return
    }
  
    currencyLock.framesPreordered = (buildingClass.buildUnitsEnabling.map(With.projections.unit) :+ 0).max
    currencyLock.isSpent = building.isDefined
    currencyLock.acquire(this)
    if ( ! needBuilder) {
      return
    }

    val desiredZone = desiredTile.map(_.zone)
    if (building.exists(_.buildUnit.isDefined)) {
      builderLock.unitMatcher.set(new UnitMatchSpecific(Set(building.get.buildUnit.get)))
    } else if ( ! builderLock.satisfied && desiredZone.exists(_.bases.exists(_.workerCount > 5))) {
      builderLock.unitMatcher.set(UnitMatchAnd(
        UnitMatchCustom(_.zone == desiredZone.get),
        builderMatcher
      ))
    } else {
      builderLock.unitMatcher.set(builderMatcher)
    }
  
    // When building placement changes we want a builder closer to the new placement
    if (orderedTile.isDefined && orderedTile != desiredTile) {
      builderLock.release()
    }
    builderLock.unitPreference.set(UnitPreferCloseAndNotMining(desiredTile.get.pixelCenter))
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
    
    if (builderLock.satisfied) {
      val builder = builderLock.units.head
      if (building.isEmpty) {
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
          builder.agent.intend(this, new Intention { toTravel = Some(desiredTile.get.pixelCenter); canAttack = false })
        } else {
          orderedTile = desiredTile
          builder.agent.intend(this, new Intention {
            toBuild     = if (currencyLock.satisfied) Some(buildingClass) else None
            toBuildTile = if (currencyLock.satisfied) orderedTile         else None
            toTravel    = Some(orderedTile.get.pixelCenter)
            canAttack   = false
          })
        }
      }
      else if (buildingClass.isTerran) {
        builder.agent.intend(this, new Intention {
          toFinish = building
          canAttack = false
        })
      }
    }
  }
  
  private def acquireDesiredTile(): Option[Tile] = {
    if (building.isDefined) {
      With.groundskeeper.flagFulfilled(buildingDescriptor, building.get)
      building.map(_.tileTopLeft)
    }
    else if (currencyLock.satisfied && currencyLock.expectedFrames < With.blackboard.maxFramesToSendAdvanceBuilder) {
      With.groundskeeper.demand(buildingDescriptor)
    }
    else {
      With.groundskeeper.require(buildingDescriptor)
    }
  }
  
  def needBuilder: Boolean = {
    if (building.isDefined) {
      return buildingClass.isTerran
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
    val travelFrames = (if (builderLock.units.isEmpty) 1.4 else 1.25) * proposedBuilder.get.framesToTravelTo(desiredTile.get.pixelCenter)
    travelFrames + 48 >= currencyLock.expectedFrames
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
