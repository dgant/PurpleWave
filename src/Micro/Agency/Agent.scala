
package Micro.Agency

import Debugging.Visualizations.ForceMap
import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Physics.ForceMath
import Mathematics.Points.{Pixel, Tile}
import Micro.Actions.{Action, Idle}
import Micro.Coordination.Pushing.{TrafficPriorities, TrafficPriority}
import Performance.{Cache, KeyedCache}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import Utilities.ByOption

import scala.collection.mutable.ArrayBuffer

class Agent(val unit: FriendlyUnitInfo) {
  
  def intend(client: Any, intent: Intention) {
    this.intent = intent
    this.client = Some(client)
  }

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
  var toScan        : Option[Pixel]             = None
  var toGather      : Option[UnitInfo]          = None
  var toAddon       : Option[UnitClass]         = None
  var toBuild       : Option[UnitClass]         = None
  var toBuildTile   : Option[Tile]              = None
  var toTrain       : Option[UnitClass]         = None
  var toTech        : Option[Tech]              = None
  var toFinish      : Option[UnitInfo]          = None
  var toUpgrade     : Option[Upgrade]           = None
  var toBoard       : Option[FriendlyUnitInfo]  = None
  var toNuke        : Option[Pixel]             = None
  var toRepair      : Option[UnitInfo]          = None
  var canFight      : Boolean                   = true
  var canFlee       : Boolean                   = true
  var canMeld       : Boolean                   = false
  var canLiftoff    : Boolean                   = false
  var canCancel     : Boolean                   = false
  var shouldEngage  : Boolean                   = false
  val forces        : ForceMap                  = new ForceMap

  /////////////////
  // Suggestions //
  /////////////////

  def destination: Pixel = toTravel.orElse(toReturn).getOrElse(origin)
  def origin: Pixel = toReturn.getOrElse(originCache())
  private val originCache = new KeyedCache(
    () =>
      ride.filterNot(unit.transport.contains).map(_.pixel)
      .orElse(toReturn)
      .orElse(
        ByOption.minBy(
          With.geography.ourBases.filter(base =>
            base.scoutedByEnemy
            || base.isNaturalOf.exists(_.scoutedByEnemy)
            || base == With.geography.ourNatural
            || base == With.geography.ourMain))(base =>
          unit.pixelDistanceTravelling(base.heart)
          // Retreat into main
          + (if (base.isNaturalOf.filter(_.owner.isUs).exists(_.heart.altitude >= base.heart.altitude) && unit.battle.exists(_.enemy.centroidGround().base == base)) 32 * 40 else 0))
        .map(_.heart.pixelCenter))
      .getOrElse(With.geography.home.pixelCenter),
    () => unit.agent.toReturn)


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
    updateRidesharing()
    Idle.consider(unit)
  }

  def decreaseImpatience(): Unit = {
    impatience = Math.max(0, impatience - With.framesSince(lastFrame))
  }

  def increaseImpatience(): Unit = {
    impatience += 2 * With.framesSince(lastFrame)
  }

  private def resetState() {
    forces.clear()
    lastPath = None
    unit.agent.priority = TrafficPriorities.None
    fightReason = ""
    tryingToMove = false
    actionsPerformed.clear()
    _rideGoal = None
  }

  private def followIntent() {
    toTravel      = intent.toTravel
    toReturn      = intent.toReturn
    toAttack      = intent.toAttack
    toScan        = intent.toScan
    toGather      = intent.toGather
    toAddon       = intent.toAddon
    toBuild       = intent.toBuild
    toBuildTile   = intent.toBuildTile
    toTrain       = intent.toTrain
    toTech        = intent.toTech
    toFinish      = intent.toFinish
    toUpgrade     = intent.toUpgrade
    toRepair      = intent.toRepair
    toBoard       = intent.toBoard.orElse(toBoard)
    toNuke        = intent.toNuke
    canFight      = intent.canFight
    canFlee       = intent.canFlee
    canMeld       = intent.canMeld
    canLiftoff    = intent.canLiftoff
    canCancel     = intent.canCancel
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
  val receivedPushPriority = new Cache(() => ByOption.max(receivedPushForces().view.map(_._1.priority)).getOrElse(TrafficPriorities.None))

  /////////////
  // Leading //
  /////////////

  val leader = new Cache(() => unit.squad.flatMap(_.leader(unit.unitClass)))
  var leadFollower: (FriendlyUnitInfo) => Unit = x => {}

  /////////////////
  // Ridesharing //
  /////////////////

  private var rideAge: Int = 0
  private var _ride: Option[FriendlyUnitInfo] = None
  private val _passengers: ArrayBuffer[FriendlyUnitInfo] = new ArrayBuffer[FriendlyUnitInfo]
  def ride: Option[FriendlyUnitInfo] = _ride
  def passengers: Seq[FriendlyUnitInfo] = (_passengers ++ unit.loadedUnits).distinct
  def claimPassenger(passenger: FriendlyUnitInfo): Unit = {
    passenger.agent._ride = Some(unit)
    passenger.agent.rideAge = 0
    if ( ! _passengers.contains(passenger)) {
      _passengers += passenger
    }
  }
  def releasePassenger(passenger: FriendlyUnitInfo): Unit = {
    passenger.agent._ride = passenger.agent._ride.filterNot(_ == unit)
    _passengers -= passenger
  }
  def updateRidesharing(): Unit = {
    if (_passengers.nonEmpty) {
      _passengers --= passengers.filter(u => !u.alive || !u.isOurs)
    }
    if (unit.transport.isDefined) {
      unit.transport.foreach(_.agent.claimPassenger(unit))
    } else {
      rideAge += 1
      if (rideAge > 2 || _ride.exists(!_.alive)) {
        _ride.foreach(_.agent.releasePassenger(unit))
      }
    }
  }
  private var _rideGoal: Option[Pixel] = None
  def rideGoal: Option[Pixel] = _rideGoal
  def directRide(to: Pixel): Unit = {
    _rideGoal = Some(to)
  }
  def prioritizedPassengers: Seq[FriendlyUnitInfo] = {
    passengers.sortBy(p => p.unitClass.subjectiveValue - p.frameDiscovered / 10000.0)
  }
}
