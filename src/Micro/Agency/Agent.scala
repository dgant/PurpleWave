
package Micro.Agency

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Physics.{ForceMap, ForceMath}
import Mathematics.Points.Pixel
import Micro.Actions.Combat.Decisionmaking.Combat
import Micro.Coordination.Pushing.{TrafficPriorities, TrafficPriority}
import Performance.{Cache, KeyedCache}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable.ArrayBuffer

class Agent(val unit: FriendlyUnitInfo) extends DestinationStack with AgencySortOrder {

  /////////////
  // History //
  /////////////

  var lastFrame     : Int = 0
  var lastStim      : Int = 0
  var lastCloak     : Int = 0
  var run           : Int = 0
  var tryingToMove  : Boolean = false // TODO: Can we infer this from destinations?

  ///////////////
  // Decisions //
  ///////////////

  var toNuke        : Option[Pixel]             = None
  var toAttack      : Option[UnitInfo]          = None
  var toAttackLast  : Option[UnitInfo]          = None
  var toGather      : Option[UnitInfo]          = None
  var toBoard       : Option[FriendlyUnitInfo]  = None
  var shouldFight   : Boolean                   = false
  var commit        : Boolean                   = false
  var wantsUnload   : Boolean                   = false
  var wantsPickup   : Boolean                   = false
  var priority      : TrafficPriority           = TrafficPriorities.Freedom
  val forces        : ForceMap                  = new ForceMap
  val combat        : Combat                    = new Combat(unit)

  /////////////////
  // Diagnostics //
  /////////////////

  var fightReason : String           = ""
  var lastAction  : Option[String]   = None
  val actions     : ArrayBuffer[String] = new ArrayBuffer[String]()

  def act(value: String): Unit = {
    actions += value
    lastAction = Some(value)
  }

  ///////////////
  // Execution //
  ///////////////

  def execute(): Unit = {
    if ( ! unit.isFriendly) return // Mind Control

    forces.clear()
    resetDestinations()

    run           += 1
    lastFrame     = With.frame
    toAttackLast  = toAttack
    priority      = TrafficPriorities.Freedom
    fightReason   = ""
    tryingToMove  = false
    wantsUnload   = false
    wantsPickup   = false
    actions.clear()
    unit.orderTarget.foreach(_.removeDamage(unit))

    _rideGoal = None
    ride.filterNot(_.alive).foreach(_.agent.removePassenger(unit))
    passengers.view.filter(u => ! u.alive || ! u.isOurs || u.unitClass.isBuilding).foreach(removePassenger)
    unit.loadedUnits.filterNot(_passengers.contains).foreach(addPassenger)

    toAttack  = unit.intent.toAttack
    toGather  = unit.intent.toGather
    toBoard   = unit.intent.toBoard.orElse(toBoard)
    toNuke    = unit.intent.toNuke
    unit.intent.action(unit)
  }

  /////////////
  // Pushing //
  /////////////

  def escalatePriority(newPriority: TrafficPriority): Unit = if (priority < newPriority && ! unit.flying) priority = newPriority
  val receivedPushForces = new Cache(() => With.coordinator.pushes
    .get(unit)
    .map(p => (p, p.force(unit)))
    .filter(_._2.exists(_.lengthSquared > 0))
    .map(p => (p._1, p._2.get))
    .toVector)
  val receivedPushForce = new KeyedCache(() => ForceMath.sum(receivedPushForces().view.filter(_._1.priority >= priority).map(_._2)), () => priority)
  val receivedPushPriority = new KeyedCache(() => Maff.max(receivedPushForces().view.map(_._1.priority)).getOrElse(TrafficPriorities.Freedom), () => priority)

  /////////////
  // Leading //
  /////////////

  val leader = new Cache(() => unit.squad.flatMap(_.leader(unit.unitClass)))
  var leadFollower: (FriendlyUnitInfo) => Unit = x => {}

  def isLeader: Boolean = leader().contains(unit)

  /////////////////
  // Ridesharing //
  /////////////////

  private val _passengers : ArrayBuffer[FriendlyUnitInfo] = new ArrayBuffer[FriendlyUnitInfo]
  private var _ride       : Option[FriendlyUnitInfo]      = None
  private var _rideGoal   : Option[Pixel]                 = None

  def passengers            : Seq[FriendlyUnitInfo]     = (_passengers ++ unit.loadedUnits).distinct
  def passengersPrioritized : Seq[FriendlyUnitInfo]     = passengers.sortBy(p => - p.unitClass.subjectiveValue - p.energy / 250.0 + p.frameDiscovered / 1e6)
  def hailers               : Seq[FriendlyUnitInfo]     = passengers            .filter(p => ! p.loaded && p.agent.wantsPickup)
  def hailersPrioritized    : Seq[FriendlyUnitInfo]     = passengersPrioritized .filter(p => ! p.loaded && p.agent.wantsPickup)
  def passengerSize         : Int                       = passengers.view.map(_.unitClass.spaceRequired).sum
  def ride                  : Option[FriendlyUnitInfo]  = _ride
  def rideGoal              : Option[Pixel]             = _rideGoal
  def isPrimaryPassenger    : Boolean                   = ride.exists(_.agent.passengersPrioritized.headOption.contains(unit))

  def addPassenger(passenger: FriendlyUnitInfo): Unit = {
    if (passengerSize + passenger.unitClass.spaceRequired > 8 && ! passenger.transport.contains(unit)) return
    passenger.agent.ride.foreach(_.agent.removePassenger(passenger))
    passenger.agent._ride = Some(unit)
    _passengers -= passenger
    _passengers += passenger
  }
  def removePassenger(passenger: FriendlyUnitInfo): Unit = {
    passenger.agent._ride = passenger.agent._ride.filter(_ != unit)
    _passengers -= passenger
  }
  def removeAllPassengers(): Unit = {
    val all = passengers.toVector
    all.foreach(removePassenger)
  }
  def setRideGoal(to: Pixel): Unit = {
    _rideGoal = Some(to)
  }

  ///////////
  // Other //
  ///////////

  def isScout: Boolean = unit.intent.toScoutTiles.nonEmpty

  def choosePerch(): Destination = perch.set(perch.pixel.orElse(toAttack.map(unit.pixelToFireAtExhaustive)))

  override def toString: String = f"${lastAction.mkString} ${toAttack.map(t => f"$t ").mkString}${super.toString}"
}
