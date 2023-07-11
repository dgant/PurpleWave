
package Micro.Agency

import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Physics.{ForceMap, ForceMath}
import Mathematics.Points.Pixel
import Micro.Actions.Combat.Decisionmaking.Combat
import Micro.Coordination.Pushing.{TrafficPriorities, TrafficPriority}
import Performance.{Cache, KeyedCache}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?

import scala.collection.mutable.ArrayBuffer

class Agent(val unit: FriendlyUnitInfo) {

  /////////////
  // History //
  /////////////

  var lastFrame     : Int = 0
  var lastStim      : Int = 0
  var lastCloak     : Int = 0
  var tryingToMove  : Boolean = false

  ///////////////
  // Decisions //
  ///////////////

  var toTravel      : Option[Pixel]             = None
  var toReturn      : Option[Pixel]             = None
  var toNuke        : Option[Pixel]             = None
  var toAttackFrom  : Option[Pixel]             = None
  var toAttack      : Option[UnitInfo]          = None
  var toAttackLast  : Option[UnitInfo]          = None
  var toGather      : Option[UnitInfo]          = None
  var toRepair      : Option[UnitInfo]          = None
  var toBoard       : Option[FriendlyUnitInfo]  = None
  var shouldFight   : Boolean                   = false
  var commit        : Boolean                   = false
  var wantsUnload   : Boolean                   = false
  var priority      : TrafficPriority           = TrafficPriorities.None
  val forces        : ForceMap                  = new ForceMap
  val combat        : Combat                    = new Combat(unit)

  /////////////////
  // Suggestions //
  /////////////////

  // Ideally consistent with the Intention logic
  def destination: Pixel = toTravel
    .orElse(toBoard.map(_.pixel))
    .orElse(toAttackFrom)
    .orElse(toAttack.orElse(toGather).orElse(toRepair).orElse(unit.intent.toFinish).map(unit.pixelToFireAtSimple))
    .orElse(toNuke)
    .orElse(unit.intent.toBuildTile.map(_.center))
    .orElse(unit.intent.toScoutTiles.headOption.map(_.center))
    .getOrElse(safety)
  def safety: Pixel = ride.filterNot(unit.transport.contains).map(_.pixel)
    .orElse(toReturn)
    .getOrElse(home)
  def home: Pixel = _home()
  private val _home = new Cache[Pixel](() =>
    Maff.minBy(
      With.geography.ourBases.filter(base =>
        base.scoutedByEnemy
        || base.naturalOf.exists(_.scoutedByEnemy)
        || base == With.geography.ourNatural
        || base == With.geography.ourMain))(base =>
      unit.pixelDistanceTravelling(base.heart)
      // Retreat into main
      + ?(base.naturalOf.filter(_.isOurs).exists(_.heart.altitude >= base.heart.altitude) && unit.battle.exists(_.enemy.centroidGround.base.contains(base)), 32 * 40, 0))
    .map(_.heart.center)
    .getOrElse(With.geography.home.center))

  def isScout: Boolean = unit.intent.toScoutTiles.nonEmpty

  /////////////////
  // Diagnostics //
  /////////////////

  var fightReason : String              = ""
  var lastPath    : Option[TilePath]    = None
  var lastAction  : Option[String]      = None
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
    lastFrame     = With.frame
    toAttackLast  = toAttack
    toAttackFrom  = None
    lastPath      = None
    priority      = TrafficPriorities.None
    fightReason   = ""
    tryingToMove  = false
    wantsUnload   = false
    actions.clear()
    unit.orderTarget.foreach(_.removeDamage(unit))

    _rideGoal = None
    ride.filterNot(_.alive).foreach(_.agent.removePassenger(unit))
    passengers.view.filter(u => ! u.alive || ! u.isOurs || u.unitClass.isBuilding).foreach(removePassenger)
    unit.loadedUnits.filterNot(_passengers.contains).foreach(addPassenger)

    toTravel  = unit.intent.toTravel
    toReturn  = unit.intent.toReturn
    toAttack  = unit.intent.toAttack
    toGather  = unit.intent.toGather
    toRepair  = unit.intent.toRepair
    toBoard   = unit.intent.toBoard.orElse(toBoard)
    toNuke    = unit.intent.toNuke
    unit.intent.action.apply(unit)
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
  val receivedPushPriority = new KeyedCache(() => Maff.max(receivedPushForces().view.map(_._1.priority)).getOrElse(TrafficPriorities.None), () => priority)

  /////////////
  // Leading //
  /////////////

  val leader = new Cache(() => unit.squad.flatMap(_.leader(unit.unitClass)))
  var leadFollower: (FriendlyUnitInfo) => Unit = x => {}

  def isLeader: Boolean = leader().contains(unit)

  /////////////////
  // Ridesharing //
  /////////////////

  private var _ride: Option[FriendlyUnitInfo] = None
  private val _passengers: ArrayBuffer[FriendlyUnitInfo] = new ArrayBuffer[FriendlyUnitInfo]
  private var _rideGoal: Option[Pixel] = None
  def ride: Option[FriendlyUnitInfo] = _ride
  def passengers: Seq[FriendlyUnitInfo] = (_passengers ++ unit.loadedUnits).distinct
  def passengersPrioritized: Seq[FriendlyUnitInfo] = passengers.sortBy(p => p.unitClass.subjectiveValue + p.energy / 250.0 - p.frameDiscovered / 10000.0 / 10.0)
  def passengerSize: Int = passengers.view.map(_.unitClass.spaceRequired).sum
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
  def rideGoal: Option[Pixel] = _rideGoal
  def setRideGoal(to: Pixel): Unit = { _rideGoal = Some(to) }
  def isPrimaryPassenger: Boolean = unit.transport.exists(_.agent.passengersPrioritized.headOption.contains(unit))

  ///////////////
  // Targeting //
  ///////////////

  def chooseAttackFrom(): Option[Pixel] = {
    toAttackFrom = toAttackFrom.orElse(toAttack.map(unit.pixelToFireAtExhaustive))
    toAttackFrom
  }
}
