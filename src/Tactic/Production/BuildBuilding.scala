package Tactic.Production

import Information.Counting.MacroCounter
import Lifecycle.With
import Macro.Requests.RequestBuildable
import Mathematics.Maff
import Mathematics.Points.{Tile, TileRectangle}
import Micro.Agency.Intention
import Performance.Cache
import Placement.Access.{Foundation, PlacementQuery}
import Placement.Architecture.ArchitecturalAssessment
import Planning.ResourceLocks.{LockCurrencyFor, LockTiles, LockUnits}
import ProxyBwapi.Races.Neutral
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.Forever
import Utilities.UnitCounters.CountOne
import Utilities.UnitPreferences.PreferClose

class BuildBuilding(requestArg: RequestBuildable, expectedFramesArg: Int) extends Production {
  setRequest(requestArg, expectedFramesArg)
  val buildingClass   : UnitClass       = request.unit.get
  val builderMatcher  : UnitClass       = buildingClass.whatBuilds._1
  val placementQuery  : PlacementQuery  = requestArg.placement.getOrElse(new PlacementQuery(buildingClass))
  val tileLock        : LockTiles       = new LockTiles(this)
  val currencyLock    : LockCurrencyFor = new LockCurrencyFor(this, buildingClass, 1)
  val builderLock     : LockUnits       = new LockUnits(this)
  builderLock.matcher = builderMatcher
  builderLock.counter = CountOne
  builderLock.interruptable = false

  var orderedTile : Option[Tile]        = None
  var foundation  : Option[Foundation]  = None
  var intendAfter : Option[Int]         = None
  private var _trainee: Option[FriendlyUnitInfo] = None

  private val proposedBuilder = new Cache(() => builderLock.inquire().flatMap(_.headOption))
  def builder: Option[FriendlyUnitInfo] = builderLock.units.headOption
  def desiredTile: Option[Tile] = trainee.map(_.tileTopLeft).orElse(foundation.map(_.tile))
  override def trainee: Option[FriendlyUnitInfo] = _trainee
  override def hasSpent: Boolean = trainee.isDefined
  override def isComplete: Boolean = trainee.exists(b => MacroCounter.countComplete(b)(buildingClass) > 0)
  override def expectTrainee(candidate: FriendlyUnitInfo): Boolean = {
    var output = orderedTile.contains(candidate.tileTopLeft) && buildingClass(candidate)
    output ||= candidate.buildUnit.exists(builder.contains)
    if (output) { _trainee = Some(candidate) }
    output
  }

  override def onUpdate(): Unit = {
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
      .filter(u => placementQuery.acceptExisting(u.tileTopLeft))
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
      if (expectedFrames > With.blackboard.maxBuilderTravelFrames()) return
      if (request.placement.isEmpty) {
        placementQuery.resetDefaults(buildingClass) // Reset placement preferences, in case eg. we no longer control some base
      }
      lazy val candidateFoundations = placementQuery.foundations
      var candidateIndex = 0
      do {
        if (foundation.isEmpty || builder.isEmpty) {
          if (candidateIndex >= candidateFoundations.length) return
          foundation = Some(candidateFoundations(candidateIndex))
          candidateIndex += 1
        }
        foundation = foundation.filter(f => With.architecture.assess(f.tile, buildingClass, expectedFrames) == ArchitecturalAssessment.Accepted)
        foundation = foundation.filter(f => With.groundskeeper.isFree(f.tile, buildingClass.tileWidthPlusAddon, buildingClass.tileHeight))
        foundation = foundation.filter(placementQuery.accept)
      } while (foundation.isEmpty)
      if (foundation.isEmpty) return
      With.architecture.assumePlacement(foundation.get.tile, buildingClass, expectedFrames) // Mainly needed so we know what will be powered in the future
      if ( ! tileLock.acquireTiles(new TileRectangle(foundation.get.tile, buildingClass.tileWidthPlusAddon, buildingClass.tileHeight).tiles)) {
        With.logger.warn(f"Failed to acquire tiles for $this at ${foundation.get.tile}")
        return
      }
    }

    if (desiredTile.isEmpty) return
    if (request.minStartFrame > 0 && request.minStartFrame > With.frame + Math.max(builderTravelFrames, incomeFrames)) return
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
    
    if (intendAfter.isDefined) {
      if (With.frame < intendAfter.get) return
      orderedTile = None
      intendAfter = None
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
          intendAfter = Some(With.frame + 24)
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
      } else if (buildingClass.isTerran) {
        builder.get.intend(this, new Intention {
          toFinishConstruction = trainee
          canFight = false
        })
      }
    }
  }

  def builderTravelFramesMax            : Double = With.blackboard.maxBuilderTravelFrames() * (1 + Maff.fromBoolean(desiredTile.get.base.exists( ! _.owner.isUs)))
  def builderTravelFrames               : Double = proposedBuilder().map(_.framesToTravelTo(desiredTile.get.center)).getOrElse(Forever()).toDouble
  def builderTravelHysteresisFrames     : Double = if (builder.isDefined) 48 else 24
  def builderTravelHysteresisMultiplier : Double = if (builder.isDefined) 1.35 else 1.2
  def builderAdvanceFrames              : Double = builderTravelHysteresisFrames + builderTravelHysteresisMultiplier * builderTravelFrames
  def incomeFrames                      : Double = Math.max(
    Maff.nanToN(if (buildingClass.mineralPrice  == 0) 0 else buildingClass.mineralPrice / With.accounting.ourIncomePerFrameMinerals, Forever()),
    Maff.nanToN(if (buildingClass.gasPrice      == 0) 0 else buildingClass.gasPrice     / With.accounting.ourIncomePerFrameGas, Forever()))

  def needBuilder: Boolean = {
    if (trainee.isDefined)                                    return buildingClass.isTerran
    if (desiredTile.isEmpty)                                  return false
    if (currencyLock.expectedFrames > builderTravelFramesMax) return false
    if (proposedBuilder().isEmpty)                            return false
    builderAdvanceFrames >= currencyLock.expectedFrames
  }
}
