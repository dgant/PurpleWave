
package Micro.Agency

import Debugging.Visualizations.ForceMap
import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Physics.ForceMath
import Mathematics.Points.Pixel
import Micro.Actions.{Action, Idle}
import Micro.Coordination.Pushing.{TrafficPriorities, TrafficPriority}
import Performance.{Cache, KeyedCache}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable.ArrayBuffer

class Agent(val unit: FriendlyUnitInfo) {

  /////////////
  // History //
  /////////////

  var lastFrame             : Int = 0
  var lastStim              : Int = 0
  var lastCloak             : Int = 0
  var fightHysteresisFrames : Int = 0
  var impatience            : Int = 0

  var tryingToMove: Boolean = false

  ///////////////
  // Decisions //
  ///////////////

  var priority      : TrafficPriority           = TrafficPriorities.None
  var toTravel      : Option[Pixel]             = None
  var toReturn      : Option[Pixel]             = None
  var toAttack      : Option[UnitInfo]          = None
  var toGather      : Option[UnitInfo]          = None
  var toBoard       : Option[FriendlyUnitInfo]  = None
  var toNuke        : Option[Pixel]             = None
  var toRepair      : Option[UnitInfo]          = None
  var shouldEngage  : Boolean                   = false
  val forces        : ForceMap                  = new ForceMap

  /////////////////
  // Suggestions //
  /////////////////

  def destination: Pixel = toTravel
    .orElse(toBoard.map(_.pixel))
    .orElse(toAttack.orElse(toGather).orElse(toRepair).orElse(intent.toFinishConstruction).map(unit.pixelToFireAt))
    .orElse(toNuke)
    .orElse(intent.toBuildTile.map(_.center))
    .orElse(intent.toScoutTiles.headOption.map(_.center))
    .getOrElse(origin)
  def origin: Pixel = toReturn.getOrElse(defaultOrigin)
  def defaultOrigin: Pixel = defaultOriginCache()
  private val originCache = new KeyedCache(
    () =>
      ride.filterNot(unit.transport.contains).map(_.pixel)
      .orElse(unit.matchups.anchor.filter(_.matchups.threats.nonEmpty).map(a => a.pixel.project(unit.battle.map(_.enemy.centroidOf(unit)).getOrElse(a.pixel), AnchorMargin(unit))))
      .orElse(toReturn)
      .getOrElse(defaultOrigin),
    () => unit.agent.toReturn)
  private val defaultOriginCache = new Cache[Pixel](() =>
    Maff.minBy(
      With.geography.ourBases.filter(base =>
        base.scoutedByEnemy
        || base.isNaturalOf.exists(_.scoutedByEnemy)
        || base == With.geography.ourNatural
        || base == With.geography.ourMain))(base =>
      unit.pixelDistanceTravelling(base.heart)
      // Retreat into main
      + (if (base.isNaturalOf.filter(_.owner.isUs).exists(_.heart.altitude >= base.heart.altitude) && unit.battle.exists(_.enemy.centroidGround().base.contains(base))) 32 * 40 else 0))
    .map(_.heart.center)
    .getOrElse(With.geography.home.center))

  def isScout: Boolean = intent.toScoutTiles.nonEmpty
  def safetyMargin: Double = 64 - Math.min(0, unit.confidence()) * 256
  def withinSafetyMargin: Boolean = unit.matchups.pixelsOfEntanglement < -safetyMargin

  /////////////////
  // Diagnostics //
  /////////////////

  var intent      : Intention           = new Intention
  var client      : Option[Any]         = None
  var lastPath    : Option[TilePath]    = None
  var lastAction  : Option[String]      = None
  var fightReason : String              = ""

  def act(value: String): Unit = { lastAction = Some(value) }

  val actionsPerformed: ArrayBuffer[Action] = new ArrayBuffer[Action]()

  ///////////////
  // Execution //
  ///////////////

  def execute() {
    if ( ! unit.isFriendly) return // Mind Control
    lastFrame = With.frame
    resetState()
    followIntent()
    fightHysteresisFrames = Math.max(0, fightHysteresisFrames - With.framesSince(lastFrame))
    decreaseImpatience()
    updatePassengers()
    Idle.consider(unit)
  }
  private def resetState() {
    forces.clear()
    lastPath = None
    unit.agent.priority = TrafficPriorities.None
    fightReason = ""
    tryingToMove = false
    actionsPerformed.clear()
    _rideGoal = None
    unit.orderTarget.foreach(_.removeDamage(unit))
  }

  def decreaseImpatience(): Unit = {
    impatience = Math.max(0, impatience - With.framesSince(lastFrame))
  }

  def increaseImpatience(): Unit = {
    impatience += 2 * With.framesSince(lastFrame)
  }

  private def followIntent() {
    toTravel      = intent.toTravel
    toReturn      = intent.toReturn
    toAttack      = intent.toAttack
    toGather      = intent.toGather
    toRepair      = intent.toRepair
    toBoard       = intent.toBoard.orElse(toBoard)
    toNuke        = intent.toNuke
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
  val receivedPushForce = new KeyedCache(() => ForceMath.sum(receivedPushForces().view.map(_._2)), () => priority)
  val receivedPushPriority = new Cache(() => Maff.max(receivedPushForces().view.map(_._1.priority)).getOrElse(TrafficPriorities.None))

  /////////////
  // Leading //
  /////////////

  val leader = new Cache(() => ride.flatMap(_.agent.prioritizedPassengers.headOption).orElse(unit.squad.flatMap(_.leader(unit.unitClass))))
  var leadFollower: (FriendlyUnitInfo) => Unit = x => {}

  /////////////////
  // Ridesharing //
  /////////////////

  private var _ride: Option[FriendlyUnitInfo] = None
  private val _passengers: ArrayBuffer[FriendlyUnitInfo] = new ArrayBuffer[FriendlyUnitInfo]
  def ride: Option[FriendlyUnitInfo] = _ride
  def passengers: Seq[FriendlyUnitInfo] = (_passengers ++ unit.loadedUnits).distinct
  def addPassenger(passenger: FriendlyUnitInfo): Unit = {
    if (unit.loadedUnitsSize + passenger.unitClass.spaceRequired > 8 && ! passenger.transport.contains(unit)) return
    passenger.agent._ride = Some(unit)
    _passengers -= passenger
    _passengers += passenger
  }
  def removePassenger(passenger: FriendlyUnitInfo): Unit = {
    passenger.agent._ride = passenger.agent._ride.filter(_ != unit)
    _passengers -= passenger
  }
  def updatePassengers(): Unit = {
    passengers.view.filter(u => ! u.alive || ! u.isOurs || u.unitClass.isBuilding).foreach(removePassenger)
    unit.loadedUnits.filterNot(_passengers.contains).foreach(addPassenger)
  }
  private var _rideGoal: Option[Pixel] = None
  def rideGoal: Option[Pixel] = _rideGoal
  def setRideGoal(to: Pixel): Unit = { _rideGoal = Some(to) }
  def prioritizedPassengers: Seq[FriendlyUnitInfo] = {
    passengers
      .sortBy(p => p.unitClass.subjectiveValue - p.frameDiscovered / 10000.0)
  }
}
