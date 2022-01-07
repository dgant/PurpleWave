package Tactics.Missions

import Debugging.SimpleString
import Information.Geography.Pathfinding.Types.TilePath
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.SpecificPoints
import Micro.Actions.Basic.{DoNothing, ReloadScarabs}
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Commands.Move
import Micro.Actions.Protoss.Shuttle.Shuttling
import Micro.Actions.{Action, Idle}
import Micro.Agency.{Commander, Intention}
import Micro.Coordination.Pathing.MicroPathing
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.{MatchTransport, MatchWorker}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactics.Squads.SquadAutomation
import Utilities.Time.{Minutes, Seconds}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class MissionDrop extends Mission {
  trait DropState extends SimpleString
  object Assembling extends DropState
  object Travelling extends DropState
  object Landing extends DropState
  object Raiding extends DropState
  object Evacuating extends DropState
  object Escaping extends DropState

  protected def additionalFormationConditions: Boolean
  protected def requireWorkers: Boolean = false
  protected def shouldStopRaiding: Boolean = false
  protected def shouldGoHome: Boolean = false
  protected def recruitable(unit: UnitInfo): Boolean = unit.complete && unit.matchups.threatsInRange.isEmpty
  protected def recruitablePassenger(unit: UnitInfo): Boolean = recruitable(unit) && Maff.orElse(transportLock.units, With.units.ours.filter(transportLock.matcher)).exists(_.pixelDistanceCenter(unit) < 640)
  protected val transportLock = new LockUnits(this)
  transportLock.matcher = unit => MatchTransport(unit) && recruitable(unit)
  transportLock.counter = CountOne

  val itinerary = new mutable.Queue[Base]
  val transports = new ArrayBuffer[FriendlyUnitInfo]
  val passengers = new ArrayBuffer[FriendlyUnitInfo]
  var state: DropState = Assembling
  var path: Option[TilePath] = None
  var pathItineraryBase: Base = _
  var pathValues: Array[PathValues] = Array.fill(256)(PathValues())
  case class PathValues(enemyRange: Int = 0, enemyVision: Boolean = false)

  override def reset(): Unit = {
    itinerary.clear()
    transports.clear()
    passengers.clear()
    state = Assembling
    path = None
  }

  final override protected def shouldForm: Boolean = {
    With.blackboard.wantToHarass() && With.recruiter.available.exists(transportLock.matcher) && additionalFormationConditions
  }

  final private def transition(newState: DropState): Unit = {
    if (state != newState) { With.logger.debug(f"$this transitioning from $state to $newState") }
    state = newState
  }

  final private def skipBase(base: Base): Boolean = {
    if (state == Evacuating || state == Escaping && units.exists(_.base.contains(base))) return true
    if (base.owner.isUs) return true
    if (base.heart.tileDistanceFast(SpecificPoints.tileMiddle) < 32 && ! base.zone.island) return true
    if (requireWorkers && base.heart.visible && base.units.forall(u => ! u.isEnemy || ! MatchWorker(u))) return true
    if (itinerary.size > 1 && With.frame < Minutes(10)() && ! base.owner.isEnemy) return true
    if ( ! base.owner.isEnemy && base.units.exists(u => u.likelyStillThere && u.isEnemy && u.canAttack && u.canMove && ! u.unitClass.isWorker)) return true
    if (shouldStopRaiding && units.exists(_.base.contains(base))) return true
    With.framesSince(base.lastScoutedFrame) < Seconds(90)() && ! base.owner.isEnemy
  }

  protected def populateItinerary(): Unit = {
    val eligibleBases = With.geography.bases.view
      .filter(_.mineralsLeft > 1500)
      .filterNot(_.owner.isFriendly)
      .filter(b => b.owner.isEnemy || With.framesSince(b.lastScoutedFrame) > Seconds(90)())
    val bases = Maff.orElse(eligibleBases.filter(_.owner.isEnemy), eligibleBases).toVector
    if (bases.isEmpty) return
    val targetBase = Maff.sampleWeighted[Base](bases, b =>
      if (b.owner.isNeutral) Maff.nanToOne(
        With.framesSince(b.lastScoutedFrame).toDouble
        * b.heart.groundTiles(With.scouting.enemyMuscleOrigin)
        / b.heart.groundTiles(With.scouting.mostBaselikeEnemyTile))
      else if (With.scouting.enemyMain.contains(b))     if ( ! b.owner.isZerg && With.frame > Minutes(13)()) 0 else 16 * (With.scouting.enemyProgress + 1)
      else if (With.scouting.enemyNatural.contains(b))  if ( ! b.owner.isZerg && With.frame > Minutes(16)()) 0 else 16 * (With.scouting.enemyProgress - 0.5)
      else b.heart.groundTiles(With.scouting.enemyMuscleOrigin)).get
    val itineraries = Vector(
        With.geography.itineraryCounterwise(With.geography.ourMain, targetBase),
        With.geography.itineraryClockwise(targetBase, With.geography.ourMain))
      .sortBy(_.size)
      .sortBy(_.count(_.owner.isEnemy))
    itinerary ++= itineraries.head.filter(eligibleBases.contains)
    With.logger.debug(f"$this itinerary: ${itinerary.view.map(_.toString).mkString(" -> ")}")
  }

  private def allAboard: Boolean = passengers.forall(_.transport.exists(transports.contains))
  private def allLanded: Boolean = passengers.forall( ! _.loaded)
  private def allHome: Boolean = units.forall(_.base.exists(_.owner.isUs))
  final override def run(): Unit = {
    transports --= transports.filterNot(_.alive)
    passengers --= passengers.filterNot(_.alive)
    if (duration > Seconds(45)() && state != Raiding && state != Evacuating) { terminate("Expired (not raiding)"); return }
    if (duration > Seconds(90)()) { terminate("Expired"); return }
    if (passengers.isEmpty) { terminate("No passengers left"); return }
    lazy val interruptedPassengers = passengers.filterNot(recruitablePassenger)
    if (state == Assembling && interruptedPassengers.nonEmpty) { terminate(f"Assembly interrupted: $interruptedPassengers") }
    if (transports.isEmpty) {
      if (Seq(Raiding, Evacuating).contains(state)) { passengers.filter(_.base.exists(_.owner.isEnemy)).foreach(_.agent.commit = true) } // Godspeed you poor souls
      terminate("No transports left"); return
    }
    while (itinerary.headOption.exists(skipBase)) {
      With.logger.debug(f"$this: Removing ${itinerary.head} from itinerary. Next base: ${itinerary.drop(1).headOption}")
      itinerary.dequeue()
    }
    if (itinerary.isEmpty) { terminate("Empty itinerary"); return }
    val base = itinerary.head
    vicinity =
      if (base.owner.isEnemy) base.heart.center
      else if (itinerary.size > 1) base.townHallArea.center.project(itinerary(1).townHallArea.center, Maff.max(transports.view.map(_.sightPixels)).getOrElse(0).toDouble)
      else base.heart.center

    if (shouldGoHome) {
      state match {
        case Assembling | Travelling | Landing => transition(Escaping)
        case Raiding => transition(Evacuating)
        case _ =>
      }
    }
    if (state == Assembling && allAboard)                                                                 { transition(Travelling)  }
    if (state == Travelling && transports.exists(_.base.exists(vicinity.base.contains)))                  { transition(Landing)     }
    if (state == Landing    && allLanded)                                                                 { transition(Raiding)     }
    if (Seq(Travelling, Landing, Raiding).contains(state) && ! vicinity.base.exists(itinerary.contains))  { transition(Evacuating)  }
    if (state == Evacuating && allAboard)                                                                 { transition(if (itinerary.isEmpty) Escaping else Travelling) }
    if (state == Escaping   && allHome)                                                                   { terminate("Escaped!"); return }
    transports.foreach(t => {
      t.agent.removeAllPassengers()
      passengers.foreach(t.agent.addPassenger)
      t.loadedUnits.view.filterNot(passengers.contains).foreach(Commander.unload(t, _))
    })
    state match {
      case Assembling => assemble()
      case Travelling => travel()
      case Landing    => land()
      case Raiding    => raid()
      case Evacuating => evacuate()
      case Escaping   => escape()
    }
  }
  private def assemble(): Unit = {
    transports.foreach(_.intend(this, new Intention { action = new ActionAssembleTransport }))
    passengers.foreach(_.intend(this, new Intention { action = new ActionAssemblePassenger }))
  }
  private def travel(): Unit = {
    transports.foreach(_.intend(this, new Intention { action = new ActionTravelTransport }))
    passengers.foreach(_.intend(this, new Intention { action = DoNothing }))
  }
  private def land(): Unit = {
    transports.foreach(_.intend(this, new Intention { action = new ActionLandTransport }))
    passengers.foreach(_.intend(this, new Intention { action = new ActionLandPassenger }))
  }
  protected def raid(): Unit = {
    SquadAutomation.targetRaid(this)
    transports.foreach(_.intend(this, new Intention { action = new ActionRaidTransport}))
    passengers.foreach(_.intend(this, new Intention { toTravel = Some(vicinity) }))
    passengers.foreach(_.agent.commit = true)
  }
  private def evacuate(): Unit = {
    passengers.foreach(_.agent.commit = false)
    transports.foreach(_.intend(this, new Intention { action = new ActionEvacuateTransport }))
    passengers.foreach(_.intend(this, new Intention { action = new ActionEvacuatePassenger }))
  }
  private def escape(): Unit = {
    passengers.foreach(_.agent.commit = false)
    transports.foreach(_.intend(this, new Intention { action = new ActionEscapeTransport }))
    passengers.foreach(_.intend(this, new Intention { action = DoNothing }))
  }

  private def createPath(transport: FriendlyUnitInfo): Unit = {
    val mapEdgeMarginTiles = Math.min(With.mapPixelWidth, With.mapPixelHeight) - Math.max(3, Seq(
      vicinity.tile.x,
      vicinity.tile.y,
      With.mapTileWidth - vicinity.tile.x,
      With.mapTileHeight - vicinity.tile.y).min)
    val profile = new PathfindProfile(transport.tile)
    profile.end                 = Some(vicinity.tile)
    profile.costEnemyVision     = 5 // Maybe ideally ~5 but this decreases likelihood of failing to find a path within maximum pathfind lengths
    profile.costRepulsion       = 25
    profile.costThreat          = 125
    profile.canCrossUnwalkable  = Some(true)
    profile.canEndUnwalkable    = Some(true)
    profile.endDistanceMaximum  = Math.max(0, 32 * 7 - 2 * transport.pixelDistanceCenter(vicinity)).toFloat
    profile.repulsors           = Vector(PathfindRepulsor(SpecificPoints.middle, 1.0, 32 * mapEdgeMarginTiles))
    path = Some(profile.find)
    pathItineraryBase = itinerary.headOption.orNull
    if (path.get.pathExists) {
      var i = 0
      while (i < Math.min(pathValues.length, path.get.tiles.get.length)) {
        val tile = path.get.tiles.get(i)
        pathValues(i) = PathValues(enemyRange = tile.enemyRange, enemyVision = tile.visibleToEnemy)
      }
    }
  }

  class ActionAssembleTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      passengers.foreach(transport.agent.addPassenger)
      val unloaded = passengers.filterNot(_.loaded)
      if (unloaded.isEmpty) {
        transport.agent.toTravel = Some(vicinity)
        Idle.delegate(transport)
      } else {
        val centroid = Maff.centroid(unloaded.view.map(_.pixel))
        if (transport.agent.withinSafetyMargin) {
          val toPickup = unloaded.minBy(_.pixelDistanceSquared(centroid))
          if (transport.pixelDistanceCenter(toPickup) < Shuttling.pickupRadius) {
            Commander.rightClick(transport, toPickup)
          } else {
            // Let Commander overshoot the Shuttle to keep it moving
            transport.agent.toTravel = Some(toPickup.pixel)
            Commander.move(transport)
          }
        } else {
          Retreat.delegate(transport)
        }
      }
    }
  }

  class ActionAssemblePassenger extends Action {
    override protected def perform(passenger: FriendlyUnitInfo): Unit = {
      passenger.agent.toTravel = Maff.minBy(transports.view.map(_.pixel))(passenger.pixelDistanceSquared)
      passenger.agent.toReturn = passenger.agent.toTravel
      ReloadScarabs.delegate(passenger)
      Potshot.delegate(passenger)
      Retreat.delegate(passenger)
    }
  }

  class ActionTravelTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      lazy val pathValueChanged = pathValues.indices.take(path.get.length).find(i =>
            path.get.tiles.get(i).enemyRange != pathValues(i).enemyRange
        ||  path.get.tiles.get(i).visibleToEnemy != pathValues(i).enemyVision)
      var shouldCreatePath = false
      if (path.isEmpty) {
        With.logger.debug(f"${this}: Calculating a path because none exists.")
        shouldCreatePath = true
      } else if (pathItineraryBase != itinerary.headOption.orNull) {
        With.logger.debug(f"${this}: Calculating a path because we are going to ${itinerary.headOption.orNull} instead of $pathItineraryBase.")
        shouldCreatePath = true
      } else if (pathValueChanged.isDefined) {
        val i = pathValueChanged.get
        With.logger.debug(f"${this}: Calculating a path because step #$i changed from ${pathValues(i)} to (${pathValues(i).enemyRange}, ${pathValues(i).enemyVision})")
        shouldCreatePath = true
      }
      if (shouldCreatePath) {
        createPath(transport)
      }
      if (path.exists(_.pathExists)) {
        With.logger.debug(f"$this: Following path from ${path.get.start} to ${path.get.end} via ${path.get.tiles.get.drop(1).headOption.getOrElse(path.get.end)}")
        MicroPathing.tryMovingAlongTilePath(transport, path.get)
      } else {
        With.logger.debug(f"$this: No path available to $vicinity")
        transport.agent.toTravel = Some(vicinity)
        transport.agent.toReturn = Some(vicinity)
        if (transport.matchups.pixelsOfEntanglement > -64) {
          Retreat.delegate(transport)
        }
        Move.delegate(transport)
      }
    }
  }

  class ActionLandTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      val runway = itinerary.headOption.map(_.heart.center).getOrElse(vicinity)
      val droppables = transport.loadedUnits
        .filter(passenger =>
          transport.doomFrameAbsolute < transport.topSpeed * transport.pixelDistanceCenter(runway) + Seconds(3)()
          || passenger.pixelDistanceCenter(runway) <= Math.max(64, passenger.effectiveRangePixels - 96)
          || (passenger.pixelDistanceCenter(runway) < passenger.effectiveRangePixels
            && passenger.matchups.threats.exists(t => t.inRangeToAttack(passenger, runway) && ! t.inRangeToAttack(passenger))))
        .sortBy(_.subjectiveValue * (if (transport.tile.enemyRange > 0) -1 else 1))
      droppables.headOption.foreach(Commander.unload(transport, _))
      transport.agent.toTravel = Some(runway)
      Commander.move(transport)
    }
  }

  class ActionLandPassenger extends Action {
    override protected def perform(unit: FriendlyUnitInfo): Unit = {
      if ( ! unit.loaded) Idle.delegate(unit)
    }
  }

  class ActionRaidTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      if (passengers.isEmpty) new ActionEscapeTransport().delegate(transport) else {
        transport.agent.toTravel = Some(Maff.centroid(Maff.orElse(passengers.view.filterNot(_.loaded), passengers.view).map(_.pixel)))
        Commander.move(transport)
      }
    }
  }

  class ActionEvacuateTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      val unloaded = passengers.filterNot(_.loaded)
      if (unloaded.isEmpty) new ActionEscapeTransport().delegate(transport) else {
        Commander.rightClick(transport, unloaded.minBy(_.pixelDistanceSquared(Maff.centroid(unloaded.view.map(_.pixel)))))
      }
    }
  }

  class ActionEvacuatePassenger extends Action {
    override protected def perform(passenger: FriendlyUnitInfo): Unit = {
      if (transports.isEmpty) {
        passenger.agent.commit = true
        Idle.delegate(passenger)
      } else {
        ReloadScarabs.delegate(passenger)
        Commander.rightClick(passenger, transports.minBy(_.pixelDistanceSquared(passenger)))
      }
    }
  }

  class ActionEscapeTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      transport.agent.toTravel = Some(transport.agent.home)
      Retreat.delegate(transport)
      Commander.move(transport)
    }
  }
}
