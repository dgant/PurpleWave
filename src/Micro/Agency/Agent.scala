
package Micro.Agency

import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.{Pixel, Tile}
import Micro.Actions.{Action, Idle}
import Micro.Coordination.Pushing.{TrafficPriorities, TrafficPriority}
import Performance.Cache
import Planning.Plan
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import Utilities.ByOption
import bwapi.Color

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Agent(val unit: FriendlyUnitInfo) {
  
  def intend(client: Plan, intent: Intention) {
    lastIntent = intent
    lastClient = Some(client)
  }
  
  /////////////
  // History //
  /////////////
  
  var combatHysteresisFrames: Int = 0
  
  ///////////////
  // Decisions //
  ///////////////

  var priority      : TrafficPriority               = TrafficPriorities.None
  var toStep        : Option[Pixel]                 = None
  var toTravel      : Option[Pixel]                 = None
  var toReturn      : Option[Pixel]                 = None
  var toAttack      : Option[UnitInfo]              = None
  var toScan        : Option[Pixel]                 = None
  var toGather      : Option[UnitInfo]              = None
  var toAddon       : Option[UnitClass]             = None
  var toBuild       : Option[UnitClass]             = None
  var toBuildTile   : Option[Tile]                  = None
  var toTrain       : Option[UnitClass]             = None
  var toTech        : Option[Tech]                  = None
  var toFinish      : Option[UnitInfo]              = None
  var toUpgrade     : Option[Upgrade]               = None
  var toLeash       : Option[Leash]                 = None
  var toBoard       : Option[FriendlyUnitInfo]      = None
  var toNuke        : Option[Pixel]                 = None
  var toRepair      : Option[UnitInfo]              = None
  var canFight      : Boolean                       = true
  var canFlee       : Boolean                       = true
  var canCower      : Boolean                       = false
  var canMeld       : Boolean                       = false
  var canPillage    : Boolean                       = false
  var canLiftoff    : Boolean                       = false
  var canCast       : Boolean                       = false
  var canCancel     : Boolean                       = false
  var canFocus      : Boolean                       = false

  var lastStim: Int = 0
  var lastCloak: Int = 0
  var shouldEngage: Boolean = false
  val forces: mutable.Map[Color, Force] = new mutable.HashMap[Color, Force]

  def isScout: Boolean = lastIntent.toScoutTiles.nonEmpty

  /////////////////
  // Suggestions //
  /////////////////

  def destination: Pixel = toTravel.orElse(toReturn).getOrElse(origin)
  def origin: Pixel = toReturn.orElse(toLeash.map(_.pixelCenter)).getOrElse(originCache())
  private val originCache = new Cache(() => toTravel
    .flatMap(_.base)
    .filter(_.owner.isUs)
    .orElse(ByOption.minBy(With.geography.ourBases)(base => unit.pixelDistanceTravelling(base.heart) * (if (base.scoutedByEnemy || base.isNaturalOf.exists(_.scoutedByEnemy)) 1 else 1000)))
    .map(_.heart.pixelCenter)
    .getOrElse(With.geography.home.pixelCenter))

  /////////////////
  // Diagnostics //
  /////////////////

  var lastFrame   : Int             = 0
  var lastIntent  : Intention       = new Intention
  var lastClient  : Option[Plan]    = None
  var lastAction  : Option[Action]  = None

  val actionsPerformed: ArrayBuffer[Action] = new ArrayBuffer[Action]()

  var fightReason: String = ""

  ///////////////
  // Execution //
  ///////////////

  def execute() {
    if ( ! unit.isFriendly) return // Mind Control
    lastFrame = With.frame
    resetState()
    followIntent()
    combatHysteresisFrames = Math.max(0, combatHysteresisFrames - With.framesSince(lastFrame))
    updateRidesharing()
    Idle.consider(unit)
  }

  private def resetState() {
    forces.clear()
    toStep = None
    fightReason = ""
    actionsPerformed.clear()
    _umbrellas.clear()
    _rideGoal = None
  }

  private def followIntent() {
    val intent    = lastIntent
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
    toLeash       = intent.toLeash
    toRepair      = intent.toRepair
    toBoard       = intent.toBoard.orElse(toBoard)
    toNuke        = intent.toNuke
    canFight      = intent.canAttack
    canFlee       = intent.canFlee
    canMeld       = intent.canMeld
    canLiftoff    = intent.canLiftoff
    canCancel     = intent.canCancel
    canFocus      = intent.canFocus
    canCast       = false
  }

  //////////////////////
  // Arbiter coverage //
  //////////////////////

  private var _umbrellas = new ArrayBuffer[FriendlyUnitInfo]
  def addUmbrella(umbrella: FriendlyUnitInfo): Unit = {
    _umbrellas += umbrella
  }
  def umbrellas: Seq[FriendlyUnitInfo] = _umbrellas

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
