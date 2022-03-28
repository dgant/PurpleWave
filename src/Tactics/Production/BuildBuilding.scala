package Tactics.Production

import Lifecycle.With
import Macro.Architecture.PlacementRequests.PlacementRequest
import Macro.Buildables.Buildable
import Macro.Scheduling.MacroCounter
import Mathematics.Maff
import Mathematics.Points.Tile
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrencyFor, LockUnits}
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.{Match, MatchAnd, MatchSpecific}
import Planning.UnitPreferences.PreferCloseAndNotMining
import ProxyBwapi.Races.Neutral
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class BuildBuilding(buildableBuilding: Buildable) extends Production {

  setBuildable(buildableBuilding)
  val buildingClass   : UnitClass       = buildable.unit.get
  val builderMatcher  : UnitClass       = buildingClass.whatBuilds._1
  val currencyLock    : LockCurrencyFor = new LockCurrencyFor(this, buildingClass, 1)
  val builderLock     : LockUnits       = new LockUnits(this)
  builderLock.interruptable = false
  builderLock.matcher = builderMatcher
  builderLock.counter = CountOne

  private var orderedTile : Option[Tile]                = None
  private var building    : Option[FriendlyUnitInfo]    = None
  private var placement   : Option[PlacementRequest]    = None
  private var waitForBuilderToRecallUntil: Option[Int]  = None

  def desiredTile: Option[Tile] = building.map(_.tileTopLeft).orElse(placement.flatMap(_.tile))

  override def hasSpent: Boolean = building.isDefined
  override def isComplete: Boolean = building.exists(b => MacroCounter.countComplete(b)(buildingClass) > 0)
  override def expectUnit(unit: FriendlyUnitInfo): Boolean = {
    if (orderedTile.contains(unit.tileTopLeft) && buildingClass(unit)) {
      building = Some(unit)
      true
    } else false
  }

  override def onCompletion(): Unit = {
    placement.foreach(p => With.groundskeeper.consume(p.blueprint, building.get))
  }

  override def onUpdate() {
    lazy val possibleBuildings = With.units.ours.filter(u =>
      buildingClass(u)
      && ! u.complete
      && MacroCounter.countComplete(u)(buildingClass) == 0
      && u.producer.forall(p => p == this || ! With.prioritizer.isPrioritized(p)))

    building = building
      // Remove dead buildings
      .filter(b => b.alive && ! Neutral.Geyser(b))
      // Take any matching incomplete building; preferably being produced by existing builder, and preferably on the targeted square
      .orElse(possibleBuildings.find(pb => pb.buildUnit.exists(_.friendly.exists(builderLock.units.contains))))
      .orElse(possibleBuildings.find(pb => orderedTile.contains(pb.tileTopLeft)))
      .orElse(possibleBuildings.find(pb => desiredTile.contains(pb.tileTopLeft)))
      .orElse(Maff.minBy(possibleBuildings)(_.frameDiscovered))
    building.foreach(_.friendly.foreach(_.setProducer(this)))

    if (building.isEmpty) {
      placement = Some(With.groundskeeper.request(this, buildingClass)).filter(_.tile.isDefined)
    }

    // Reserve money if we have a place to build
    if (desiredTile.isDefined && ! hasSpent) {
      currencyLock.framesPreordered = (buildingClass.buildUnitsEnabling.map(With.projections.unit) :+ 0).max
      currencyLock.acquire()
    }

    if ( ! needBuilder) {
      builderLock.release()
      return
    }

    // Find an appropriate builder (or make sure we use the current builder)
    val desiredZone = desiredTile.map(_.zone)
    if (building.exists(_.buildUnit.isDefined)) {
      builderLock.matcher = new MatchSpecific(Set(building.get.buildUnit.get))
    } else if ( ! builderLock.satisfied && desiredZone.exists(_.bases.exists(_.workerCount > 5))) {
      builderLock.matcher = MatchAnd(Match(_.zone == desiredZone.get), builderMatcher)
    } else {
      builderLock.matcher = builderMatcher
    }
  
    // When building placement changes we want a builder closer to the new placement
    if (orderedTile.isDefined && orderedTile != desiredTile) {
      builderLock.release()
    }
    builderLock.preference = PreferCloseAndNotMining(desiredTile.get.center)
    builderLock.acquire()
    
    if (waitForBuilderToRecallUntil.isDefined) {
      if (With.frame < waitForBuilderToRecallUntil.get) return
      orderedTile = None
      waitForBuilderToRecallUntil = None
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
          builder.intend(this, new Intention {
            toTravel    = desiredTile.map(_.center)
            toBuildTile = desiredTile
            canFight    = false })
        } else {
          orderedTile = desiredTile
          builder.intend(this, new Intention {
            toBuild     = if (hasSpent || currencyLock.satisfied) Some(buildingClass) else None
            toBuildTile = orderedTile
            toTravel    = orderedTile.map(_.center)
            canFight    = false
          })
        }
        desiredTile.foreach(With.groundskeeper.reserve(this, _, buildingClass))
      } else if (buildingClass.isTerran) {
        builder.intend(this, new Intention {
          toFinishConstruction = building
          canFight = false
        })
      }
    }
  }

  def needBuilder: Boolean = {
    lazy val proposedBuilder = builderLock.inquire().flatMap(_.headOption)
    lazy val maxFramesToSendAdvanceBuilder = With.blackboard.maxFramesToSendAdvanceBuilder * (1 + Maff.fromBoolean(desiredTile.get.base.exists( ! _.owner.isUs) && proposedBuilder.isDefined))
    if (building.isDefined) {
      return buildingClass.isTerran
    } else if (desiredTile.isEmpty) {
      return false
    } else if (currencyLock.expectedFrames > maxFramesToSendAdvanceBuilder) {
      return false
    }
    if (proposedBuilder.isEmpty) {
      return false
    }
    val travelFrames                = proposedBuilder.get.framesToTravelTo(desiredTile.get.center)
    val travelHysteresisFrames      = if (builderLock.units.nonEmpty) 48 else 24
    val travelHysteresisMultiplier  = if (builderLock.units.nonEmpty) 1.35 else 1.2
    travelHysteresisFrames + travelHysteresisMultiplier * travelFrames >= currencyLock.expectedFrames
  }
}
