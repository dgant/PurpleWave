package Tactics.Missions

import Debugging.SimpleString
import Information.Geography.Pathfinding.{PathfindProfile, PathfindRepulsor}
import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.SpecificPoints
import Micro.Actions.{Action, Idle}
import Micro.Actions.Basic.DoNothing
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Agency.{Commander, Intention}
import Micro.Coordination.Pathing.MicroPathing
import Planning.UnitMatchers.{MatchTransport, MatchWorker}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Seconds

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

  final override protected def shouldTerminate: Boolean = itinerary.isEmpty || duration > Seconds(75)() && state != Raiding

  final private def transition(newState: DropState): Unit = {
    if (state != newState) { With.logger.debug(f"$this transitioning from $state to $newState") }
    state = newState
  }

  final private def skipBase(base: Base): Boolean = {
    if (With.framesSince(base.lastScoutedFrame) < Seconds(45)()) return false
    if ( ! base.owner.isEnemy) return true
    if (requireWorkers && base.heart.visible && base.units.forall(u => ! u.isEnemy || ! MatchWorker(u))) return true
    false
  }

  final override def run(): Unit = {
    transports --= transports.view.filterNot(_.alive)
    passengers --= passengers.view.filterNot(_.alive)
    if (passengers.isEmpty) { terminate(); return }
    if (transports.isEmpty) {
      if (Seq(Raiding, Evacuating).contains(state)) { passengers.foreach(_.agent.commit) }
      terminate()
      return
    }
    while (itinerary.headOption.exists(skipBase)) { itinerary.dequeue() }
    itinerary.headOption.foreach(b => vicinity = b.heart.center)
    if (itinerary.isEmpty) { terminate(); return }

    if (state == Recruiting)                                                                { transition(Assembling)  }
    if (state == Assembling && passengers.forall(_.transport.exists(transports.contains)))  { transition(Travelling)  }
    if (state == Travelling && passengers.forall(_.base.exists(vicinity.base.contains)))    { transition(Landing)     }
    if (state == Landing    && passengers.forall( ! _.loaded))                              { transition(Raiding)     }
    if (state == Raiding    && shouldStopRaiding)                                           { transition(Evacuating)  }
    if (state == Evacuating && passengers.forall(_.loaded))                                 { transition(Escaping)    }
    if (state == Escaping   && units.forall(u => u.base.exists(_.owner.isUs) || ! u.visibleToOpponents)) { terminate(); return }
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
    passengers.foreach(_.intend(this, new Intention { action = DoNothing }))
  }
  private def evacuate(): Unit = {
    transports.foreach(_.intend(this, new Intention { action = new ActionEvacuateTransport }))
    passengers.foreach(_.intend(this, new Intention { action = new ActionEvacuatePassenger }))
  }
  private def escape(): Unit = {
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
      profile.repulsors           = Vector(PathfindRepulsor(SpecificPoints.middle, 1.0, Math.min(With.mapPixelWidth, With.mapPixelHeight) / 2))
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
