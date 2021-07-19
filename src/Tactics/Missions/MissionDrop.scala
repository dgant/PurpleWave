package Tactics.Missions

import Debugging.SimpleString
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.SpecificPoints
import Micro.Actions.Basic.DoNothing
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.{Action, Idle}
import Micro.Agency.{Commander, Intention}
import Micro.Coordination.Pathing.MicroPathing
import Planning.UnitMatchers.{MatchTransport, MatchWorker}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.{Minutes, Seconds}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class MissionDrop extends Mission {
  trait DropState extends SimpleString
  object Recruiting extends DropState
  object Assembling extends DropState
  object Travelling extends DropState
  object Landing extends DropState
  object Raiding extends DropState
  object Evacuating extends DropState
  object Escaping extends DropState

  protected def additionalFormationConditions: Boolean
  protected def shouldStopRaiding: Boolean = false
  protected def requireWorkers: Boolean = false
  protected def raid(): Unit

  val itinerary = new mutable.Queue[Base]
  val transports = new ArrayBuffer[FriendlyUnitInfo]
  val passengers = new ArrayBuffer[FriendlyUnitInfo]
  var state: DropState = Recruiting

  override def reset(): Unit = {
    itinerary.clear()
    transports.clear()
    passengers.clear()
    state = Recruiting
  }

  final override protected def shouldForm: Boolean = {
    With.blackboard.wantToHarass() && With.units.existsOurs(MatchTransport) && additionalFormationConditions
  }

  final private def transition(newState: DropState): Unit = {
    if (state != newState) { With.logger.debug(f"$this transitioning from $state to $newState") }
    state = newState
  }

  final private def skipBase(base: Base): Boolean = {
    if (state == Evacuating || state == Escaping && units.exists(_.base.contains(base))) return true
    if (base.owner.isUs) return true
    if (requireWorkers && base.heart.visible && base.units.forall(u => ! u.isEnemy || ! MatchWorker(u))) return true
    if (itinerary.size > 1 && With.frame < Minutes(10)() && ! base.owner.isEnemy) return true
    if ( ! base.owner.isEnemy && base.units.exists(u => u.likelyStillThere && u.isEnemy)) return true
    With.framesSince(base.lastScoutedFrame) < Seconds(45)() && ! base.owner.isEnemy
  }

  protected def populateItinerary(): Unit = {
    val bases = Maff.orElse(
      With.geography.enemyBases,
      With.geography.neutralBases.filter(b => With.framesSince(b.lastScoutedFrame) > Minutes(2)()),
      With.geography.neutralBases).toVector
    if (bases.isEmpty) return
    val targetBase = Maff.sampleWeighted[Base](bases, b =>
      if (b.owner.isNeutral) With.framesSince(b.lastScoutedFrame)
      else if (b.mineralsLeft < 5000) 10
      else if (With.scouting.enemyMain.contains(b)) if (With.frame > Minutes(12)()) 0 else 25
      else if (With.scouting.enemyNatural.contains(b)) if (With.frame > Minutes(16)()) 0 else 10
      else b.heart.tileDistanceGroundManhattan(With.scouting.enemyMuscleOrigin)).get
    val itineraries = Vector(With.geography.itineraryCounterwise(With.geography.ourMain, targetBase), With.geography.itineraryClockwise(targetBase, With.geography.ourMain))
      .sortBy(_.size)
      .sortBy(_.count(_.owner.isEnemy))
    itinerary ++= itineraries.head
  }

  final protected def updateVicinity(): Unit = {
    if (itinerary.isEmpty) {
      vicinity = With.geography.home.center
      return
    }
    val base = itinerary.head
    lazy val sightPixels = Maff.max(transports.view.map(_.sightPixels)).getOrElse(0)
    vicinity =
      if (base.owner.isEnemy) base.heart.center
      else if (itinerary.size > 1) base.townHallArea.center.project(itinerary(1).townHallArea.center, sightPixels)
      else base.heart.center
  }

  final override def run(): Unit = {
    transports --= transports.view.filterNot(_.alive)
    passengers --= passengers.view.filterNot(_.alive)
    if (duration > Seconds(75)() && state != Raiding && state != Evacuating) { terminate("Expired"); return }
    if (passengers.isEmpty) { terminate("No passengers left"); return }
    if (transports.isEmpty) {
      // Godspeed you poor souls
      if (Seq(Raiding, Evacuating).contains(state)) { passengers.foreach(_.agent.commit = true) }
      terminate("No transports left"); return
    }
    while (itinerary.headOption.exists(skipBase)) { itinerary.dequeue() }
    if (itinerary.isEmpty) { terminate("Empty itinerary"); return }
    updateVicinity()


    if (state == Recruiting)                                                                { transition(Assembling)  }
    if (state == Assembling && passengers.forall(_.transport.exists(transports.contains)))  { transition(Travelling)  }
    if (state == Travelling && passengers.forall(_.base.exists(vicinity.base.contains)))    { transition(Landing)     }
    if (state == Landing    && passengers.forall( ! _.loaded))                              { transition(Raiding)     }
    if (state == Raiding    && ! vicinity.base.exists(itinerary.contains))                  { transition(Evacuating)  }
    if (state == Raiding    && shouldStopRaiding)                                           { transition(Evacuating)  }
    if (state == Evacuating && passengers.forall(_.loaded))                                 { transition(if (itinerary.isEmpty) Escaping else Travelling) }
    if (state == Escaping   && units.forall(u => u.base.exists(_.owner.isUs) || ! u.visibleToOpponents)) { terminate("Escaped!"); return }
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
    passengers.foreach(_.intend(this, new Intention { action = new ActionAssembleTransport }))
  }
  private def travel(): Unit = {
    transports.foreach(_.intend(this, new Intention { action = new ActionTravelTransport }))
    passengers.foreach(_.intend(this, new Intention { action = DoNothing }))
  }
  private def land(): Unit = {
    transports.foreach(_.intend(this, new Intention { action = new ActionLandTransport }))
    passengers.foreach(_.intend(this, new Intention { action = new ActionLandPassenger }))
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

  class ActionAssembleTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      val unloaded = passengers.filterNot(_.loaded)
      if (unloaded.isEmpty) Idle.consider(transport) else {
        // TODO: Avoid danger
        val centroid = Maff.centroid(passengers.view.map(_.pixel))
        Commander.rightClick(transport, passengers.minBy(_.pixelDistanceSquared(centroid)))
      }
    }
  }

  class ActionAssemblePassenger extends Action {
    override protected def perform(passenger: FriendlyUnitInfo): Unit = {
      Potshot.delegate(passenger)
      if (transports.isEmpty) Idle.consider(passenger) else Commander.rightClick(passenger, transports.minBy(_.pixelDistanceSquared(passenger)))
    }
  }

  class ActionTravelTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      val profile = new PathfindProfile(transport.tile)
      profile.end                 = Some(vicinity.tile)
      profile.endDistanceMaximum  = 7 * 32
      profile.costEnemyVision     = 5
      profile.costRepulsion       = 25
      profile.costThreat          = 125
      profile.canCrossUnwalkable  = Some(true)
      profile.canEndUnwalkable    = Some(true)
      profile.repulsors           = Vector(PathfindRepulsor(SpecificPoints.middle, 1.0, Math.min(With.mapPixelWidth, With.mapPixelHeight) * 2 / 3))
      val path = profile.find
      if (path.pathExists) { MicroPathing.tryMovingAlongTilePath(transport, path) }
      else {
        With.logger.debug(f"$this: No path available to $vicinity")
        transport.agent.toTravel = Some(vicinity)
        transport.agent.toReturn = Some(vicinity)
        Retreat.consider(transport)
      }
    }
  }

  class ActionLandTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      val droppables = transport.loadedUnits
        .filter(p =>
          p.pixelDistanceCenter(vicinity) <= Math.max(0, p.effectiveRangePixels - 64)
          || (p.canAttack && transport.tile.enemyRangeAir >= With.grids.enemyRangeAir.margin)
          || p.matchups.targetsInRange.exists(t => t.unitClass.isWorker || t.canAttack))
        .sortBy(_.subjectiveValue * (if (transport.tile.enemyRangeAir > 0) -1 else 1))
      droppables.headOption.foreach(Commander.unload(transport, _))
      transport.agent.toTravel = Some(vicinity)
      Commander.move(transport)
    }
  }

  class ActionLandPassenger extends Action {
    override protected def perform(unit: FriendlyUnitInfo): Unit = {
      if ( ! unit.loaded) Idle.consider(unit)
    }
  }

  class ActionRaidTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      if (passengers.isEmpty) new ActionEscapeTransport().consider(transport) else {
        transport.agent.toTravel = Some(Maff.centroid(Maff.orElse(passengers.view.filterNot(_.loaded), passengers.view).map(_.pixel)))
        Commander.move(transport)
      }
    }
  }

  class ActionEvacuateTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      val unloaded = passengers.filterNot(_.loaded)
      if (unloaded.isEmpty) new ActionEscapeTransport().consider(transport) else {
        Commander.rightClick(transport, unloaded.minBy(_.pixelDistanceSquared(Maff.centroid(unloaded.view.map(_.pixel)))))
      }
    }
  }

  class ActionEvacuatePassenger extends Action {
    override protected def perform(passenger: FriendlyUnitInfo): Unit = {
      if (transports.isEmpty) {
        passenger.agent.commit = true
        Idle.consider(passenger)
      } else {
        Commander.rightClick(passenger, transports.minBy(_.pixelDistanceSquared(passenger)))
      }
    }
  }

  class ActionEscapeTransport extends Action {
    override protected def perform(transport: FriendlyUnitInfo): Unit = {
      transport.agent.toTravel = Some(With.geography.home.center)
      Retreat.consider(transport)
      Commander.move(transport)
    }
  }
}
