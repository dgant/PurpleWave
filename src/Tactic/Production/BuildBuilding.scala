package Tactic.Production

import Lifecycle.With
import Macro.Requests.RequestBuildable
import Macro.Scheduling.MacroCounter
import Mathematics.Maff
import Mathematics.Points.Tile
import Micro.Agency.Intention
import Placement.Access.{Foundation, PlacementQuery}
import Planning.ResourceLocks.{LockCurrencyFor, LockUnits}
import ProxyBwapi.Races.Neutral
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.UnitCounters.CountOne
import Utilities.UnitPreferences.PreferClose

class BuildBuilding(requestArg: RequestBuildable) extends Production {
  setRequest(requestArg)
  val buildingClass   : UnitClass       = request.unit.get
  val builderMatcher  : UnitClass       = buildingClass.whatBuilds._1
  val currencyLock    : LockCurrencyFor = new LockCurrencyFor(this, buildingClass, 1)
  val builderLock     : LockUnits       = new LockUnits(this)
  builderLock.matcher = builderMatcher
  builderLock.counter = CountOne
  builderLock.interruptable = false
  val placementQuery  : PlacementQuery  = Maff.??(requestArg.tileFilter.asInstanceOf[PlacementQuery], new PlacementQuery)
  placementQuery.requirements.width   = Some(buildingClass.tileWidth)
  placementQuery.requirements.height  = Some(buildingClass.tileHeight)
  placementQuery.preferences.width    = placementQuery.preferences.width
  placementQuery.preferences.height   = placementQuery.preferences.height
  placementQuery.preferences.building = Some(buildingClass)
  placementQuery.preferences.zone     = With.geography.ourZones
  placementQuery.preferences.base     = With.geography.ourBases

  private var orderedTile : Option[Tile]                = None
  private var foundation   : Option[Foundation]         = None
  private var waitForBuilderToRecallUntil: Option[Int]  = None

  def builder: Option[FriendlyUnitInfo] = builderLock.units.headOption
  def desiredTile: Option[Tile] = trainee.map(_.tileTopLeft).orElse(foundation.map(_.tile))

  private var _trainee: Option[FriendlyUnitInfo] = None
  override def trainee: Option[FriendlyUnitInfo] = _trainee
  override def hasSpent: Boolean = trainee.isDefined
  override def isComplete: Boolean = trainee.exists(b => MacroCounter.countComplete(b)(buildingClass) > 0)
  override def expectTrainee(candidate: FriendlyUnitInfo): Boolean = {
    var output = orderedTile.contains(candidate.tileTopLeft) && buildingClass(candidate)
    output ||= candidate.buildUnit.exists(builder.contains)
    if (output) { _trainee = Some(candidate) }
    output
  }

  override def onUpdate() {
    // Populate the trainee manually.
    // This step may rarely be necessary because the expectTrainee() process can theoretically fail when:
    // 1. We command a builder to build at X
    // 2. We command a builder to build at Y
    // 3. While the second command hasn't been executed due to latency, the builder constructs a building at X
    lazy val underConstructionByBuilder = builder
      .flatMap(_.buildUnit)
      .flatMap(_.friendly)
      .filter(buildingClass)
      .filter(_.producer.contains(this))
    lazy val candidates = builder.map(knownBuilder => With.units.ours
      .filter(buildingClass)
      .filterNot(_.complete)
      .filter(_.producer.forall(==))
      .filter(_.pixelDistanceEdge(knownBuilder) < 32 * 4)
      .filter(candidate => request.tileFilter(candidate.tileTopLeft))
      .filter(MacroCounter.countComplete(_)(buildingClass) == 0)).getOrElse(Seq.empty)
    _trainee = _trainee
      .filter(_.alive)
      .filterNot(Neutral.Geyser)
      .orElse(underConstructionByBuilder)
      .orElse(candidates.find(candidate => orderedTile.contains(candidate.tileTopLeft)))
      .orElse(candidates.find(candidate => desiredTile.contains(candidate.tileTopLeft)))
      .orElse(Maff.minBy(candidates)(_.frameDiscovered))
    trainee.foreach(_.friendly.foreach(_.setProducer(this)))
    orderedTile = trainee.map(_.tileTopLeft).orElse(orderedTile)

    if (trainee.isEmpty) {
      //With.architecture.buildable()
    }

    if (desiredTile.isEmpty) return
    if ( ! hasSpent) { currencyLock.acquire() }
    if ( ! needBuilder) {
      builderLock.release()
      return
    }

    // Find an appropriate builder (or make sure we use the current builder)
    val desiredZone = desiredTile.map(_.zone)
    if (trainee.exists(_.buildUnit.isDefined)) {
      builderLock.matcher = trainee.get.buildUnit.contains
    } else if ( ! builderLock.satisfied && desiredZone.exists(_.bases.exists(_.workerCount > 5))) {
      // Performance optimization: Only consider workers in the same zone when we have a lot available
      builderLock.matcher = unit => desiredZone.contains(unit.zone) && builderMatcher(unit)
    } else {
      builderLock.matcher = builderMatcher
    }
  
    // When building placement changes we want a builder closer to the new placement
    if (orderedTile.isDefined && orderedTile != desiredTile) {
      builderLock.release()
    }
    builderLock.preference = PreferClose(desiredTile.get.center)
    builderLock.acquire()
    
    if (waitForBuilderToRecallUntil.isDefined) {
      if (With.frame < waitForBuilderToRecallUntil.get) return
      orderedTile = None
      waitForBuilderToRecallUntil = None
    }
    
    if (builderLock.satisfied) {
      if (trainee.isEmpty) {
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
          builder.get.intend(this, new Intention {
            toTravel    = desiredTile.map(_.center)
            toBuildTile = desiredTile
            canFight    = false })
        } else {
          orderedTile = desiredTile
          builder.get.intend(this, new Intention {
            toBuild     = if (hasSpent || currencyLock.satisfied) Some(buildingClass) else None
            toBuildTile = orderedTile
            toTravel    = orderedTile.map(_.center)
            canFight    = false
          })
        }
        // TODO: Use locks instead
        //desiredTile.foreach(With.groundskeeper.reserve(this, _, buildingClass))
      } else if (buildingClass.isTerran) {
        builder.get.intend(this, new Intention {
          toFinishConstruction = trainee
          canFight = false
        })
      }
    }
  }

  def needBuilder: Boolean = {
    lazy val proposedBuilder = builderLock.inquire().flatMap(_.headOption)
    lazy val maxBuilderTravelFrames = With.blackboard.maxBuilderTravelFrames() * (1 + Maff.fromBoolean(desiredTile.get.base.exists( ! _.owner.isUs) && proposedBuilder.isDefined))
    if (trainee.isDefined)                                    return buildingClass.isTerran
    if (desiredTile.isEmpty)                                  return false
    if (currencyLock.expectedFrames > maxBuilderTravelFrames) return false
    if (proposedBuilder.isEmpty)                              return false
    val travelFrames                = proposedBuilder.get.framesToTravelTo(desiredTile.get.center)
    val travelHysteresisFrames      = if (builder.isDefined) 48 else 24
    val travelHysteresisMultiplier  = if (builder.isDefined) 1.35 else 1.2
    val builderAdvanceFrames        = travelHysteresisFrames + travelHysteresisMultiplier * travelFrames
    builderAdvanceFrames >= currencyLock.expectedFrames
  }
}
