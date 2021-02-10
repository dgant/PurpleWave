package Planning.Plans.Macro.Build

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Macro.Buildables.{Buildable, BuildableUnit}
import Macro.Scheduling.MacroCounter
import Macro.Architecture.PlacementRequests.PlacementRequest
import Mathematics.Points.Tile
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyForUnit, LockUnits}
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers.{MatchAnd, Match, MatchSpecific}
import Planning.UnitPreferences.PreferCloseAndNotMining
import ProxyBwapi.Races.Neutral
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

class BuildBuilding(val buildingClass: UnitClass) extends Production {

  override def producerCurrencyLocks: Seq[LockCurrency] = Seq(currencyLock)
  override def producerUnitLocks: Seq[LockUnits] = Seq(builderLock)
  override def producerInProgress: Boolean = building.isDefined
  override def buildable: Buildable = BuildableUnit(buildingClass)

  private var orderedTile : Option[Tile]              = None
  private var building    : Option[FriendlyUnitInfo]  = None
  
  val currencyLock = new LockCurrencyForUnit(buildingClass)

  val builderMatcher: UnitClass = buildingClass.whatBuilds._1
  val builderLock: LockUnits = new LockUnits
  builderLock.unitCounter.set(UnitCountOne)
  builderLock.unitMatcher.set(builderMatcher)
  builderLock.interruptable.set(false)
    
  var waitForBuilderToRecallUntil: Option[Int] = None
  var placement: Option[PlacementRequest] = None

  def desiredTile: Option[Tile] = building.map(_.tileTopLeft).orElse(placement.flatMap(_.tile))

  override def isComplete: Boolean = building.exists(b => MacroCounter.countComplete(b)(buildingClass) > 0)

  override def onCompletion(): Unit = {
    placement.foreach(p => With.groundskeeper.consume(p.blueprint, building.get))
  }

  override def onUpdate() {

    lazy val possibleBuildings = With.units.ours.filter(u =>
      u.is(buildingClass)
      && ! u.complete
      && MacroCounter.countComplete(u)(buildingClass) == 0
      && u.getProducer.forall(p => p == this || ! With.prioritizer.isPrioritized(p)))

    building = building
      // Remove dead buildings
      .filter(b => b.alive && ! b.is(Neutral.Geyser))
      // Take any matching incomplete building; preferably being produced by existing builder, and preferably on the targeted square
      .orElse(possibleBuildings.find(pb => pb.buildUnit.flatMap(_.friendly).exists(_.friendly.exists(builderLock.units.contains))))
      .orElse(possibleBuildings.find(pb => orderedTile.contains(pb.tileTopLeft)))
      .orElse(possibleBuildings.find(pb => desiredTile.contains(pb.tileTopLeft)))
      .orElse(ByOption.minBy(possibleBuildings)(_.frameDiscovered))
    building.foreach(_.friendly.foreach(_.setProducer(this)))

    if (building.isEmpty) {
      placement = Some(With.groundskeeper.request(this, buildingClass))
    }

    // Reserve money if we have a place to build
    if (desiredTile.isDefined) {
      currencyLock.framesPreordered = (buildingClass.buildUnitsEnabling.map(With.projections.unit) :+ 0).max
      currencyLock.isSpent = building.isDefined
      currencyLock.acquire(this)
    }

    if ( ! needBuilder) {
      builderLock.release()
      return
    }

    // Find an appropriate builder (or make sure we use the current builder)
    val desiredZone = desiredTile.map(_.zone)
    if (building.exists(_.buildUnit.isDefined)) {
      builderLock.unitMatcher.set(new MatchSpecific(Set(building.get.buildUnit.get)))
    } else if ( ! builderLock.satisfied && desiredZone.exists(_.bases.exists(_.workerCount > 5))) {
      builderLock.unitMatcher.set(MatchAnd(Match(_.zone == desiredZone.get), builderMatcher))
    } else {
      builderLock.unitMatcher.set(builderMatcher)
    }
  
    // When building placement changes we want a builder closer to the new placement
    if (orderedTile.isDefined && orderedTile != desiredTile) {
      builderLock.release()
    }
    builderLock.unitPreference.set(PreferCloseAndNotMining(desiredTile.get.pixelCenter))
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
          builder.agent.intend(this, new Intention {
            toTravel    = desiredTile.map(_.pixelCenter)
            toBuildTile = desiredTile
            canFight    = false })
        } else {
          orderedTile = desiredTile
          builder.agent.intend(this, new Intention {
            toBuild     = if (currencyLock.satisfied) Some(buildingClass) else None
            toBuildTile = orderedTile
            toTravel    = orderedTile.map(_.pixelCenter)
            canFight    = false
          })
        }
        desiredTile.foreach(With.groundskeeper.reserve(this, _, buildingClass))
      }
      else if (buildingClass.isTerran) {
        builder.agent.intend(this, new Intention {
          toFinish = building
          canFight = false
        })
      }
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
}
