package Micro.Agency

import Information.Geography.Pathfinding.Types.ZonePath
import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.{Pixel, PixelRay, Tile}
import Micro.Actions.Combat.Techniques.Common.ActionTechniqueEvaluation
import Micro.Actions.{Action, Idle}
import Micro.Heuristics.Targeting.TargetingProfile
import Performance.Cache
import Planning.Plan
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import bwapi.Color

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

class Agent(val unit: FriendlyUnitInfo) {
  
  ///////////////
  // Messaging //
  ///////////////
  
  def shove(shover: FriendlyUnitInfo) {
    shovers.append(shover)
  }
  
  def intend(client: Plan, intent: Intention) {
    lastIntent = intent
    lastClient = Some(client)
  }
  
  /////////////
  // History //
  /////////////
  
  var combatHysteresisFrames: Int = 0
  var lastIntent: Intention = new Intention
  var shovers: ListBuffer[FriendlyUnitInfo] = new ListBuffer[FriendlyUnitInfo]
  
  ///////////////
  // Decisions //
  ///////////////
  
  var toReturn      : Option[Pixel]                 = None
  var toTravel      : Option[Pixel]                 = None
  var toAttack      : Option[UnitInfo]              = None
  var toScan        : Option[Pixel]                 = None
  var toGather      : Option[UnitInfo]              = None
  var toAddon       : Option[UnitClass]             = None
  var toBuild       : Option[UnitClass]             = None
  var toBuildTile   : Option[Tile]                  = None
  var toTrain       : Option[UnitClass]             = None
  var toTech        : Option[Tech]                  = None
  var toFinish      : Option[UnitInfo]              = None
  var toForm        : Option[Pixel]                 = None
  var toUpgrade     : Option[Upgrade]               = None
  var toLeash       : Option[Leash]                 = None
  var toBoard       : Option[FriendlyUnitInfo]      = None
  var toNuke        : Option[Pixel]                 = None
  var toRepair      : Option[UnitInfo]              = None
  var canFight      : Boolean                       = true
  var canFlee       : Boolean                       = true
  var canCower      : Boolean                       = false
  var canMeld       : Boolean                       = false
  var canScout      : Boolean                       = false
  var canPillage    : Boolean                       = false
  var canLiftoff    : Boolean                       = false
  var canCast       : Boolean                       = false
  var canCancel     : Boolean                       = false
  var canFocus      : Boolean                       = false
  
  var targetingProfile: TargetingProfile = TargetingProfiles.default
  
  var lastStim: Int = 0
  var lastCloak: Int = 0
  var shouldEngage: Boolean = false
  val forces: mutable.Map[Color, Force] = new mutable.HashMap[Color, Force]
  val resistances: mutable.Map[Color, Vector[Force]] = new mutable.HashMap[Color, Vector[Force]]
  
  def zonePath(to: Pixel): Option[ZonePath] = {
    if ( ! cachedZonePath.contains(to)) {
      cachedZonePath(to) = With.paths.zonePath(unit.zone, to.zone)
    }
    cachedZonePath(to)
  }
  private val cachedZonePath = new mutable.HashMap[Pixel, Option[ZonePath]]
  
  def nextWaypoint(to: Pixel): Pixel = {
    if (unit.flying) return to
    if ( ! With.strategy.map.forall(_.trustGroundDistance)) return to
    if ( ! cachedWaypoint.contains(to)) {
      val path = zonePath(to)
      cachedWaypoint(to) = path
        .map(_.steps
          .map(_.edge.pixelCenter)
          .find(step => unit.pixelDistanceCenter(step) > 128)
          .getOrElse(to))
        .getOrElse(to)
    }
    cachedWaypoint(to)
  }
  private val cachedWaypoint = new mutable.HashMap[Pixel, Pixel]
  
  /////////////////
  // Suggestions //
  /////////////////
  
  def origin: Pixel = originCache()
  private val originCache = new Cache(() => calculateOrigin)
  
  // This is kind of a mess.
  def destination: Pixel = destinationCache()
  private val destinationCache = new Cache(() => calculateDestination)
  
  /////////////////
  // Diagnostics //
  /////////////////
  
  var lastFrame   : Int             = 0
  var lastClient  : Option[Plan]    = None
  var lastAction  : Option[Action]  = None

  var movingTo: Option[Pixel] = None
  
  var pathsAll        : Traversable[PixelRay] = Seq.empty
  var pathsTruncated  : Traversable[PixelRay] = Seq.empty
  var pathsAcceptable : Traversable[PixelRay] = Seq.empty
  var pathAccepted    : Traversable[PixelRay] = Seq.empty
  
  val techniques: ArrayBuffer[ActionTechniqueEvaluation] = new ArrayBuffer[ActionTechniqueEvaluation]
  
  var fightReason: String = ""
  
  ///////////////
  // Execution //
  ///////////////
  
  def execute() {
    // ie. Mind Control
    if ( ! unit.isFriendly) return
    
    resetState()
    followIntent()
    combatHysteresisFrames = Math.max(0, combatHysteresisFrames - With.framesSince(lastFrame))
    lastFrame = With.frame
    updateRidesharing()
    Idle.consider(unit)
    cleanUp()
  }

  private def resetState() {
    forces.clear()
    resistances.clear()
    movingTo            = None
    targetingProfile    = TargetingProfiles.default
    pathsAll            = Seq.empty
    pathsTruncated      = Seq.empty
    pathsAcceptable     = Seq.empty
    pathAccepted        = Seq.empty
    cachedZonePath.clear()
    cachedWaypoint.clear()
    techniques.clear()
    fightReason = ""
    _umbrellas.clear()
  }

  private def followIntent() {
    val intent    = lastIntent
    toReturn      = intent.toReturn
    toTravel      = intent.toTravel
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
    toForm        = intent.toForm
    toNuke        = intent.toNuke
    toRepair      = None
    toBoard       = None
    canFight      = intent.canAttack
    canFlee       = intent.canFlee
    canMeld       = intent.canMeld
    canScout      = intent.canScout
    canLiftoff    = intent.canLiftoff
    canCast       = false
    canCancel     = intent.canCancel
    canFocus      = intent.canFocus
  }

  private def cleanUp() {
    shovers.clear()
  }

  private def calculateOrigin: Pixel = {
    if (toReturn.isDefined) {
      toReturn.get
    }
    else if (toForm.isDefined) {
      toForm.get
    }
    else if (toLeash.isDefined) {
      toLeash.get.pixelCenter
    }
    else if (With.geography.ourBases.nonEmpty) {
      With.geography.ourBases.map(_.heart.pixelCenter).minBy(unit.pixelDistanceTravelling)
    }
    else {
      With.geography.home.pixelCenter
    }
  }

  private def calculateDestination: Pixel = {
    if (toTravel.isDefined) {
      toTravel.get
    }
    else if (toForm.isDefined) {
      toForm.get
    }
    else {
      origin
    }
  }

  //////////////////////
  // Arbiter coverage //
  //////////////////////

  private var _umbrellas = new ArrayBuffer[FriendlyUnitInfo]
  def addUmbrella(umbrella: FriendlyUnitInfo): Unit = {
    _umbrellas += umbrella
  }
  def umbrellas: Seq[FriendlyUnitInfo] = _umbrellas

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
    _passengers --= passengers.filter(u => ! u.alive || ! u.isOurs)
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
  def consumePassengerRideGoal: Option[Pixel] = {
    val output = _rideGoal
    _rideGoal = None
    output
  }
  def setMyRideGoal(to: Pixel): Unit = {
    _rideGoal = Some(to)
  }
}
