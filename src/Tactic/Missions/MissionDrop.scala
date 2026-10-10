package Tactic.Missions

import Debugging.SimpleString
import Information.Battles.Types.CombatGroup
import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Points, Tile}
import Micro.Actions.Basic.DoNothing
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Commands.Travel
import Micro.Actions.Protoss.ReloadScarabs
import Micro.Actions.{Action, Idle}
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Orders
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactic.Squads.SquadAutomation
import Utilities.Time.{Minutes, Seconds}
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.IsWorker

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class MissionDrop extends Mission {
  private var dropCombatGroup = new CombatGroup(toString)
  override def combatGroup: CombatGroup = if (launched) dropCombatGroup else CombatGroup.army
  trait DropState   extends SimpleString
  object Assembling extends DropState
  object Travelling extends DropState
  object Landing    extends DropState
  object Raiding    extends DropState
  object Evacuating extends DropState
  object Escaping   extends DropState

  protected def additionalFormationConditions             : Boolean
  protected def additionalItineraryConditions(base: Base) : Boolean = true
  protected def requireWorkers                            : Boolean = false
  protected def refreshItinerary(): Unit = {}
  protected def continueItinerary(from: Base): Boolean = false
  protected def isScoutingTarget(base: Base): Boolean = false
  protected def canLandAt(base: Base): Boolean = true
  protected def targetCleared(base: Base): Boolean = requireWorkers && base.heart.visible && base.units.forall(u => !u.isEnemy || !IsWorker(u))
  protected def shouldStopRaiding                         : Boolean = false
  protected def shouldGoHome                              : Boolean = false
  protected def recruited(unit: UnitInfo)                 : Boolean = With.recruiter.lockedBy(this).exists(_ == unit)
  protected def recruitable(unit: UnitInfo)               : Boolean = unit.complete && (recruited(unit) || ! unit.team.exists(_.engagedUpon))
  protected def recruitablePassenger(unit: UnitInfo)      : Boolean = recruitable(unit) && (recruited(unit) || Maff.orElse(transportLock.units, With.recruiter.available.filter(transportLock.matcher)).exists(_.pixelDistanceCenter(unit) < 640))
  protected def raidTransportAction: Action = ActionRaidTransport
  protected def commitWhileRaiding: Boolean = true
  protected def defaultCombatLanding: Boolean = false
  protected def requiredPassenger(passenger: FriendlyUnitInfo): Boolean = true
  protected def landingGoal(transport: FriendlyUnitInfo): Pixel = vicinity
  protected def readyToRaid(transport: FriendlyUnitInfo): Boolean = false
  protected def onRaidHandoff(transport: FriendlyUnitInfo): Unit = {}
  protected final def threatenedOnApproach(transport: FriendlyUnitInfo, waypoint: => Option[Pixel]): Boolean =
    transport.matchups.threats.nonEmpty && !transport.matchups.withinSafetyMargin &&
      !waypoint.exists(MicroPathing.isSafeMove(transport, _))
  protected def onDestinationChanged(): Unit = {}
  protected def missionTimeout: Int = Seconds(90)()
  protected def approachTimeout: Int = Seconds(45)()
  protected val transportLock: LockUnits = new LockUnits(this, u => u.isTransport && recruitable(u)).setCounter(CountOne).setInterruptible(false)

  val itinerary                             = new mutable.Queue[Base]
  val transports                            = new ArrayBuffer[FriendlyUnitInfo]
  val passengers                            = new ArrayBuffer[FriendlyUnitInfo]
  var state             : DropState         = Assembling
  private val routes = mutable.Map.empty[FriendlyUnitInfo, DropRoute]
  private val initiallyLanded = mutable.Set.empty[FriendlyUnitInfo]
  protected val raidingTransports = mutable.Set.empty[FriendlyUnitInfo]
  private val pickupTargets = mutable.Map.empty[FriendlyUnitInfo, FriendlyUnitInfo]
  protected var destinationBase: Option[Base] = None
  private var returningHome = false
  private var itineraryFrame = 0
  private var destinationFrame = 0

  final def assignedTransport(passenger: FriendlyUnitInfo): Option[FriendlyUnitInfo] =
    passenger.transport.filter(transports.contains).orElse(passenger.agent.ride.filter(transports.contains))
  protected final def passengersFor(transport: FriendlyUnitInfo): Seq[FriendlyUnitInfo] = passengers.filter(p => assignedTransport(p).contains(transport))

  override def reset(): Unit = {
    dropCombatGroup = new CombatGroup(f"$this@${With.frame}")
    itinerary.clear()
    transports.clear()
    passengers.clear()
    state = Assembling
    routes.clear()
    initiallyLanded.clear()
    raidingTransports.clear()
    pickupTargets.clear()
    destinationBase = None
    returningHome = false
    itineraryFrame = With.frame
    destinationFrame = With.frame
  }

  final override protected def shouldForm: Boolean = {
    With.blackboard.wantToHarass() && With.recruiter.available.exists(transportLock.matcher) && additionalFormationConditions && { populateItinerary(); itinerary.nonEmpty }
  }

  final private def transition(newState: DropState): Unit = {
    if (state != newState) {
      With.logger.debug(f"$this transitioning from $state to $newState")
      pickupTargets.clear()
    }
    state = newState
  }

  private def ignore(base: Base): Boolean = {
    if (base.owner.isFriendly) return true
    if (isScoutingTarget(base)) return false
    if (!base.isEnemy && base.heart.tileDistanceFast(Points.tileMiddle) < 32 && ! base.zone.island) return true
    if (targetCleared(base)) return true
    if ( ! base.owner.isEnemy && base.units.exists(u => u.likelyStillThere && u.isEnemy && u.canAttack && u.canMove && ! u.unitClass.isWorker)) return true
    if ( ! additionalItineraryConditions(base)) return true
    With.framesSince(base.lastFrameScoutedByUs) < Seconds(90)() && ! base.owner.isEnemy
  }

  private def scoutTactics = Vector(With.tactics.scoutExpansions, With.tactics.acePilots, With.tactics.scoutWithOverlord, With.tactics.monitor, With.tactics.darkTemplar)

  final private def skipBase(base: Base): Boolean = {
    if (ignore(base)) return true
    if (isScoutingTarget(base)) return false
    if ((state == Evacuating || state == Escaping) && units.exists(_.base.contains(base))) return true
    if (base.isNeutral && itinerary.size > 1 && With.frame < Minutes(10)()) return true
    if (base.isNeutral) {
      val maxCost = if (itinerary.size > 1 && With.frame < Minutes(10)()) 0
        else if (scoutTactics.exists(t => With.recruiter.lockedBy(t).nonEmpty)) 32  * 24
        else 32 * 48
      val here  = transports.headOption.map(_.pixel).getOrElse(centroidAir)
      val there = base.townHallArea.center
      val next  = itinerary.drop(1).headOption.map(_.townHallArea.center).getOrElse(there)
      val cost = here.pixelDistance(there) + there.pixelDistance(next) - here.pixelDistance(next)
      if (cost > maxCost) return true
    }
    if (shouldStopRaiding && (units.exists(_.base.contains(base)) || (defaultCombatLanding && destinationBase.contains(base)))) return true
    false
  }

  protected def populateItinerary(): Unit = {
    val eligibleBases = With.geography.bases.view
      .filter(_.mineralsLeft > 1500)
      .filterNot(ignore)
      // COG 2022: Bots react too fast to the drop (and often cannon up)
      .filter(b => With.configuration.humanMode || ! (With.scouting.enemyMain.contains(b) || With.scouting.enemyNatural.contains(b)))
    val bases = Maff.orElse(eligibleBases.filter(_.owner.isEnemy), eligibleBases).toVector
    if (bases.isEmpty) return
    val targetBase = Maff.sampleWeighted[Base](bases, b =>
      if (b.owner.isNeutral) Maff.nanToOne(
        With.framesSince(b.lastFrameScoutedByUs).toDouble
        * Math.min(b.heart.groundTiles(With.scouting.enemyMuscleOrigin),  10 * b.heart.tileDistanceFast(With.scouting.enemyMuscleOrigin))
        / Math.min(b.heart.groundTiles(With.scouting.enemyHome),          10 * b.heart.tileDistanceFast(With.scouting.enemyHome)))
      else if (With.scouting.enemyMain.contains(b))     if ( ! b.owner.isZerg && With.frame > Minutes(13)()) 0 else 16 * (With.scouting.enemyProximity + 1)
      else if (With.scouting.enemyNatural.contains(b))  if ( ! b.owner.isZerg && With.frame > Minutes(16)()) 0 else 16 * (With.scouting.enemyProximity - 0.5)
      else Math.min(b.heart.groundTiles(With.scouting.enemyMuscleOrigin), 10 * b.heart.tileDistanceFast(With.scouting.enemyMuscleOrigin))).get
    val itineraries = Vector(
        With.geography.itineraryCounterwise(With.geography.ourMain, targetBase),
        With.geography.itineraryClockwise(With.geography.ourMain, targetBase))
      .sortBy(_.size)
      .sortBy(_.count(_.owner.isEnemy))
    itinerary.clear()
    itinerary ++= itineraries.head.filter(bases.contains)
    With.logger.debug(f"$this itinerary: ${itinerary.view.map(_.toString).mkString(" -> ")}")
  }

  private def allAboard: Boolean = passengers.forall(_.transport.exists(transports.contains))
  private def allLanded: Boolean = passengers.forall(initiallyLanded.contains)
  private def allHome: Boolean = units.forall(_.base.exists(_.owner.isUs))
  final override def run(): Unit = {
    transports --= transports.filterNot(u => u.alive && u.squad.contains(this))
    passengers --= passengers.filterNot(u => u.alive && u.squad.contains(this))
    if (passengers.isEmpty && (!defaultCombatLanding || transports.isEmpty)) { terminate("No passengers left"); return }
    if (transports.isEmpty) {
      if (Seq(Raiding, Evacuating).contains(state)) { passengers.filter(_.base.exists(_.owner.isEnemy)).foreach(_.agent.commit = true) } // Godspeed you poor souls
      terminate("No transports left"); return
    }
    updateManifest()
    if (passengers.isEmpty && !defaultCombatLanding) { terminate("No transportable passengers left"); return }
    refreshItinerary()
    while (itinerary.headOption.exists(skipBase)) {
      With.logger.debug(f"$this: Leaving target ${itinerary.head.heart}. Next target: ${itinerary.drop(1).headOption.map(_.heart)}")
      itinerary.dequeue()
    }
    val expired = With.framesSince(Math.max(launchFrame, itineraryFrame)) > missionTimeout ||
      (With.framesSince(Math.max(launchFrame, destinationFrame)) > approachTimeout && state != Raiding && state != Evacuating)
    val renewed = !returningHome && !shouldGoHome && (itinerary.isEmpty || expired) && destinationBase.exists(continueItinerary)
    if (renewed) itineraryFrame = With.frame
    updateState(shouldGoHome || (expired && !renewed))
    if (state == Escaping && allHome) { terminate("Escaped!"); return }
    transports.filter(t => state == Assembling || (state == Escaping && t.base.exists(_.owner.isUs))).foreach(t => {
      t.loadedUnits.view.filterNot(passengers.contains).foreach(p => {
        With.logger.micro(f"Drop transport $t ditching rider $p")
        Commander.unload(t, p)
      })
    })
    transports.foreach(_.intend(this).setStation(None))
    state match {
      case Assembling => assemble()
      case Travelling => travel()
      case Landing    => land()
      case Raiding    => raid()
      case Evacuating => evacuate()
      case Escaping   => escape()
    }
  }
  private def updateManifest(): Unit = {
    val current = passengers.flatMap(p => assignedTransport(p).map(p -> _)).toMap
    val assigned = DropManifest.fill(transports, passengers, current)
    transports.foreach(t => t.agent.passengers.filterNot(p => assigned.get(p).contains(t)).foreach(t.agent.removePassenger))
    assigned.foreach { case (p, t) => if (!p.agent.ride.contains(t)) t.agent.addPassenger(p) }
    releasePassengers(passengers.filterNot(assigned.contains).toSet)
    routes.keys.filterNot(transports.contains).toVector.foreach(routes.remove)
    pickupTargets.keys.filterNot(transports.contains).toVector.foreach(pickupTargets.remove)
    raidingTransports.retain(transports.contains)
  }

  private def releasePassengers(stranded: Set[FriendlyUnitInfo]): Unit = {
    if (stranded.nonEmpty) {
      // Losing capacity must not leave evacuation waiting forever for an impossible load.
      With.recruiter.locksOf(this).toVector.foreach(lock => {
        val retained = lock.units.filterNot(stranded).toVector
        if (retained.size != lock.units.size) {
          lock.release()
          With.recruiter.lockTo(lock, retained)
        }
      })
      stranded.foreach(p => {
        if (!p.loaded && p.order == Orders.EnterTransport) Commander.stop(p)
        p.agent.ride.foreach(_.agent.removePassenger(p))
        p.agent.commit = false
        p.intend(this).setAction(Idle)
      })
      passengers --= stranded
    }
  }

  private def beginDestination(): Unit = {
    destinationBase = itinerary.headOption
    destinationFrame = With.frame
    initiallyLanded.clear()
    raidingTransports.clear()
    routes.clear()
    onDestinationChanged()
    destinationBase.foreach(base => {
      vicinity =
        if (base.owner.isEnemy || isScoutingTarget(base)) base.heart.center
        else if (itinerary.size > 1) base.townHallArea.center.project(itinerary(1).townHallArea.center, Maff.max(transports.view.map(_.sightPixels)).getOrElse(0).toDouble)
        else base.heart.center
    })
  }

  private def updateState(goHome: Boolean): Unit = {
    returningHome ||= goHome || itinerary.isEmpty
    if (destinationBase.isEmpty && !returningHome) beginDestination()
    // Keep the old vicinity throughout pickup; a new destination never orders
    // grounded passengers to walk away from the Shuttle collecting them.
    if (returningHome || destinationBase != itinerary.headOption) {
      if (state != Evacuating && state != Escaping) transition(Evacuating)
    }
    if (state == Assembling && allAboard) transition(Travelling)
    if (state == Travelling && !allAboard) transition(Evacuating)
    if (state == Travelling && transports.exists(_.base.exists(b => destinationBase.contains(b) && canLandAt(b)))) transition(Landing)
    if (state == Landing) {
      if (defaultCombatLanding) {
        transports.filterNot(raidingTransports).filter(readyToRaid).foreach(t => {
          raidingTransports += t
          onRaidHandoff(t)
          With.logger.debug(s"$this handing $t and its crew to default combat at ${t.pixel}")
        })
        if (transports.forall(t => raidingTransports(t) || !passengersFor(t).exists(requiredPassenger))) transition(Raiding)
      } else {
        initiallyLanded ++= passengers.filterNot(_.loaded)
        if (allLanded) transition(Raiding)
      }
    }
    if (state == Escaping && !allAboard) transition(Evacuating)
    if (state == Evacuating && allAboard) {
      if (returningHome) transition(Escaping) else {
        beginDestination()
        transition(Travelling)
      }
    }
  }

  private def assemble(): Unit = {
    transports.foreach(t => {
      val pickup = pickupTarget(t, assembling = true)
      if (pickup.exists(t.pixelDistanceCenter(_) > 384)) intendRoute(t, pickup.get.tile)
      else t.intend(this).setAction(ActionAssembleTransport)
    })
    passengers.foreach(_.intend(this).setAction(ActionAssemblePassenger))
  }
  private def travel(): Unit = {
    transports.foreach(t => intendRoute(t, vicinity.tile))
    passengers.foreach(_.intend(this).setAction(DoNothing))
  }
  private def land(): Unit = {
    SquadAutomation.targetRaid(this)
    passengers.foreach(_.agent.commit = commitWhileRaiding)
    passengers.foreach(_.intend(this).setTerminus(vicinity))
    transports.foreach(t => {
      if (defaultCombatLanding && raidingTransports(t)) t.intend(this).setAction(raidTransportAction)
      else if (defaultCombatLanding && !passengersFor(t).exists(requiredPassenger)) intendRoute(t, t.agent.defaultHome.tile)
      else if (defaultCombatLanding || !t.base.exists(destinationBase.contains)) intendRoute(t, landingGoal(t).tile)
      else t.intend(this).setAction(ActionLandTransport)
    })
    passengers.foreach(p => p.intend(this).setAction(
      if (!defaultCombatLanding) ActionLandPassenger
      else if (assignedTransport(p).exists(raidingTransports)) Idle else DoNothing))
  }
  protected def raid(): Unit = {
    SquadAutomation.targetRaid(this)
    transports.foreach(t => {
      if (defaultCombatLanding && !passengersFor(t).exists(requiredPassenger)) intendRoute(t, t.agent.defaultHome.tile)
      else t.intend(this).setAction(raidTransportAction)
    })
    passengers.foreach(_.intend(this).setAction(Idle).setTerminus(vicinity))
    passengers.foreach(_.agent.commit = commitWhileRaiding)
  }
  private def evacuate(): Unit = {
    passengers.foreach(_.agent.commit = false)
    transports.foreach(t => {
      val pickup = pickupTarget(t, assembling = false)
      if (pickup.exists(t.pixelDistanceCenter(_) > 384)) intendRoute(t, pickup.get.tile)
      else t.intend(this).setAction(ActionEvacuateTransport)
    })
    passengers.foreach(_.intend(this).setAction(ActionEvacuatePassenger))
  }
  private def escape(): Unit = {
    passengers.foreach(_.agent.commit = false)
    transports.foreach(t => intendRoute(t, t.agent.defaultHome.tile))
    passengers.foreach(_.intend(this).setAction(DoNothing))
  }

  private def readyForPickup(passenger: FriendlyUnitInfo): Boolean =
    !Protoss.Reaver(passenger) || passenger.scarabs + passenger.trainingQueue.size >= 2

  private def pickupTarget(transport: FriendlyUnitInfo, assembling: Boolean): Option[FriendlyUnitInfo] = pickupTargets.synchronized {
    val unloaded = passengersFor(transport).filterNot(_.loaded)
      .filter(p => !assembling || readyForPickup(p))
    val required = Maff.orElse(unloaded.filter(requiredPassenger), unloaded).toVector
    val candidates = if (assembling) required else
      Maff.orElse(required.filter(_.matchups.threatsInRange.nonEmpty), required).toVector
    val selected = pickupTargets.get(transport).filter(candidates.contains)
      .orElse(Maff.minBy(candidates)(_.pixelDistanceSquared(transport)))
    selected.foreach(pickupTargets(transport) = _)
    selected
  }

  private def boardSelected(passenger: FriendlyUnitInfo, assembling: Boolean): Unit = {
    assignedTransport(passenger).foreach(t => {
      if (pickupTarget(t, assembling).contains(passenger)) Commander.rightClick(passenger, t)
      else if (passenger.order == Orders.EnterTransport) Commander.stop(passenger)
      else DoNothing.delegate(passenger)
    })
  }

  object ActionAssembleTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      val pickup = pickupTarget(transport, assembling = true)
      if (pickup.isEmpty) {
        Commander.hold(transport)
      } else {
        if (transport.matchups.withinSafetyMargin) {
          val toPickup = pickup.get
          transport.agent.decision.set(toPickup.pixel)
          Commander.rightClick(transport, toPickup)
        } else {
          Retreat.delegate(transport)
        }
      }
    }
  }

  object ActionAssemblePassenger extends Action {
    override protected def perform(passenger: FriendlyUnitInfo): Unit = {
      if (passenger.loaded) return
      passenger.agent.redoubt.set(
        passenger.agent.decision.set(assignedTransport(passenger).map(_.pixel)))
      ReloadScarabs.delegate(passenger)
      if (readyForPickup(passenger)) {
        boardSelected(passenger, assembling = true)
      }
    }
  }

  protected final def approachStalled: Boolean =
    transports.nonEmpty && transports.forall(t => routes.get(t).exists(_.stalled))

  protected final def routeWaypoint(transport: FriendlyUnitInfo, destination: Tile): Option[Pixel] = {
    val route = routes.getOrElseUpdate(transport, new DropRoute)
    route.next(transport.tile, destination, itinerary.filter(_.isEnemy).toSet).map(route.waypoint(transport.pixel, _))
  }

  private def intendRoute(transport: FriendlyUnitInfo, destination: Tile): Unit = {
    val intent = transport.intend(this).setTerminus(destination.center)
    val waypoint = routeWaypoint(transport, destination)
    if (threatenedOnApproach(transport, waypoint)) {
      intent.setAction(Retreat)
      return
    }
    val route = routes(transport)
    waypoint match {
      case Some(pixel) => intent.setStation(pixel).setAction(Travel)
      case None =>
        if (route.stalled) returningHome = true
        intent.setRedoubt(transport.agent.defaultHome).setAction(ActionAwaitRoute)
    }
  }

  private object ActionAwaitRoute extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      Retreat.delegate(transport)
      Commander.hold(transport)
    }
  }

  object ActionLandTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      if (passengersFor(transport).forall(initiallyLanded.contains)) {
        raidTransportAction.delegate(transport)
        return
      }
      val runway = destinationBase.map(_.heart.center).getOrElse(vicinity)
      val droppables = transport.loadedUnits.filterNot(initiallyLanded.contains)
        .filter(passenger =>
          transport.doomedInFrames < transport.framesToTravelTo(runway) + Seconds(3)()
          || passenger.pixelDistanceCenter(runway) <= Math.max(64, passenger.effectiveRangePixels - 96)
          || (passenger.pixelDistanceCenter(runway) < passenger.effectiveRangePixels
            && passenger.matchups.threats.exists(t => t.inRangeToAttack(passenger, runway) && ! t.inRangeToAttack(passenger))))
        .sortBy(_.subjectiveValue * (if (transport.tile.enemyRange > 0) -1 else 1))
      droppables.headOption.foreach(Commander.unload(transport, _))
      transport.agent.decision.set(runway)
      Commander.move(transport)
    }
  }

  object ActionLandPassenger extends Action {
    override protected def perform(unit: FriendlyUnitInfo): Unit = {
      if (!unit.loaded || initiallyLanded(unit)) Idle.delegate(unit)
    }
  }

  object ActionRaidTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      val crew = passengersFor(transport)
      if (crew.isEmpty) Commander.hold(transport) else {
        transport.agent.decision.set(Maff.centroid(Maff.orElse(crew.view.filterNot(_.loaded), crew.view).map(_.pixel)))
        Commander.move(transport)
      }
    }
  }

  object ActionEvacuateTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      val pickup = pickupTarget(transport, assembling = false)
      if (pickup.isEmpty) {
        if (transport.matchups.threats.nonEmpty && !transport.matchups.withinSafetyMargin) Retreat.delegate(transport)
        Commander.hold(transport)
      } else {
        Commander.rightClick(transport, pickup.get)
      }
    }
  }

  object ActionEvacuatePassenger extends Action {
    override protected def perform(passenger: FriendlyUnitInfo): Unit = {
      if (passenger.loaded) return
      boardSelected(passenger, assembling = false)
    }
  }
}
