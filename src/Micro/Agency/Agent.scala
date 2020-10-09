
package Micro.Agency

import Information.Geography.Pathfinding.PathfindProfile
import Information.Geography.Pathfinding.Types.{TilePath, ZonePath}
import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.{Pixel, PixelRay, Tile}
import Micro.Actions.{Action, Idle}
import Performance.Cache
import Planning.Plan
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import Utilities.ByOption
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
  var canPillage    : Boolean                       = false
  var canLiftoff    : Boolean                       = false
  var canCast       : Boolean                       = false
  var canCancel     : Boolean                       = false
  var canFocus      : Boolean                       = false
  
  var lastStim: Int = 0
  var lastCloak: Int = 0
  var shouldEngage: Boolean = false
  val forces: mutable.Map[Color, Force] = new mutable.HashMap[Color, Force]
  val resistances: mutable.Map[Color, Vector[Force]] = new mutable.HashMap[Color, Vector[Force]]

  def canScout: Boolean = lastIntent.toScoutTiles.nonEmpty
  
  def zonePath(to: Pixel): Option[ZonePath] = {
    if ( ! cachedZonePath.contains(to)) {
      cachedZonePath(to) = With.paths.zonePath(unit.zone, to.zone)
    }
    cachedZonePath(to)
  }
  private val cachedZonePath = new mutable.HashMap[Pixel, Option[ZonePath]]
  
  /////////////////
  // Suggestions //
  /////////////////

  def origin: Pixel = toReturn.orElse(toForm).orElse(toLeash.map(_.pixelCenter)).getOrElse(originCache())
  private val originCache = new Cache(() => toTravel
    .flatMap(_.base)
    .filter(_.owner.isUs)
    .orElse(ByOption.minBy(With.geography.ourBases)(base => unit.pixelDistanceTravelling(base.heart) * (if (base.scoutedByEnemy || base.isNaturalOf.exists(_.scoutedByEnemy)) 1 else 1000)))
    .map(_.heart.pixelCenter)
    .getOrElse(With.geography.home.pixelCenter))
  
  // This is kind of a mess.
  def destination: Pixel = destinationCache()
  private val destinationCache = new Cache(() => calculateDestination)
  
  /////////////////
  // Diagnostics //
  /////////////////
  
  var lastFrame   : Int             = 0
  var lastClient  : Option[Plan]    = None
  var lastAction  : Option[Action]  = None

  var movingTo        : Option[Pixel]         = None
  var path            : Option[TilePath]      = None
  var pathBranches    : Seq[(Pixel, Pixel)]   = Seq.empty
  var pathsAll        : Traversable[PixelRay] = Seq.empty
  var pathsTruncated  : Traversable[PixelRay] = Seq.empty
  var pathsAcceptable : Traversable[PixelRay] = Seq.empty
  var pathAccepted    : Traversable[PixelRay] = Seq.empty

  def focusPath: TilePath = {
    refreshFocusPath()
    focusPathCache()
  }
  def focusPathSteps: Seq[Tile] = {
    focusPathStepsCache()
  }
  private def refreshFocusPath(): Unit = {
    val destinationTile = destination.tileIncluding
    if ( ! focusPathGoal.contains(destinationTile)) {
      focusPathCache.invalidate()
    }
    focusPathGoal = Some(destinationTile)
  }
  private var focusPathGoal: Option[Tile] = None
  private val focusPathCache = new Cache[TilePath](() => {
    val profile = new PathfindProfile(unit.tileIncludingCenter)
    profile.end                 = focusPathGoal
    profile.canCrossUnwalkable  = unit.flying || unit.transport.exists(_.flying)
    profile.allowGroundDist     = true
    profile.costEnemyVision     = 1f
    profile.costThreat          = 2f
    profile.unit = Some(unit)
    profile.find
  })
  val focusPathStepSize: Int = 6
  val focusPathStepSizeSqrt2: Double = focusPathStepSize * Math.sqrt(2)
  private val focusPathStepsCache = new Cache[Seq[Tile]](() => {
    val path = focusPathCache()
    if (path.tiles.isEmpty) {
      Seq.empty
    } else {
      path.tiles.get.view.zipWithIndex.filter(_._2 % focusPathStepSize == 0).map(_._1)
    }
  })

  val actionsPerformed: ArrayBuffer[Action] = new ArrayBuffer[Action]()
  
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
    path                = None
    pathBranches        = Seq.empty
    pathsAll            = Seq.empty
    pathsTruncated      = Seq.empty
    pathsAcceptable     = Seq.empty
    pathAccepted        = Seq.empty
    focusPathGoal       = None
    cachedZonePath.clear()
    fightReason = ""
    actionsPerformed.clear()
    _umbrellas.clear()
    _rideGoal = None
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
    toRepair      = intent.toRepair
    toForm        = intent.toForm
    toBoard       = intent.toBoard.orElse(toBoard)
    toNuke        = intent.toNuke
    canFight      = intent.canAttack
    canFlee       = intent.canFlee
    canMeld       = intent.canMeld
    canLiftoff    = intent.canLiftoff
    canCast       = false
    canCancel     = intent.canCancel
    canFocus      = intent.canFocus
  }

  private def cleanUp() {
    shovers.clear()
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

  ///////////////////////
  // Explosion hopping //
  ///////////////////////

  var hoppingExplosion = false
}
