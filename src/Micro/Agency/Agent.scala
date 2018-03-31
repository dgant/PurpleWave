package Micro.Agency

import Information.Geography.Pathfinding.ZonePath
import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.{Pixel, PixelRay, Tile}
import Micro.Actions.Combat.Techniques.Common.ActionTechniqueEvaluation
import Micro.Actions.{Action, Idle}
import Micro.Heuristics.Targeting.TargetingProfile
import Performance.Cache
import Planning.Plan
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
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
  var canBerzerk    : Boolean                       = false
  var canLiftoff    : Boolean                       = false
  var canCast       : Boolean                       = false
  
  var targetingProfile: TargetingProfile = TargetingProfiles.default
  
  var lastStim: Int = 0
  var lastCloak: Int = 0
  var shouldEngage: Boolean = false
  val forces: mutable.Map[Color, Force] = new mutable.HashMap[Color, Force]
  
  def zonePath(to: Pixel): Option[ZonePath] = {
    if ( ! cachedZonePath.contains(to)) {
      cachedZonePath(to) = With.paths.zonePath(unit.zone, to.zone)
    }
    cachedZonePath(to)
  }
  private val cachedZonePath = new mutable.HashMap[Pixel, Option[ZonePath]]
  
  def nextWaypoint(to: Pixel): Pixel = {
    if (unit.flying) return to
    if ( ! cachedWaypoint.contains(to)) {
      val path = zonePath(to)
      cachedWaypoint(to) =
        if (path.isEmpty || path.get.steps.isEmpty)
          to
        else
          path.get.steps.head.edge.pixelCenter
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
  
  var netEngagementValue: Double = _
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
    resetState()
    followIntent()
    combatHysteresisFrames = Math.max(0, combatHysteresisFrames - With.framesSince(lastFrame))
    lastFrame = With.frame
    Idle.consider(unit)
    cleanUp()
  }
  
  private def resetState() {
    forces.clear()
    netEngagementValue  = 1.0
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
    canCower      = intent.canCower
    canMeld       = intent.canMeld
    canScout      = intent.canScout
    canPillage    = intent.canPillage
    canBerzerk    = intent.canBerzerk
    canLiftoff    = intent.canLiftoff
    canCast       = false
  }
  
  private def cleanUp() {
    shovers.clear()
  }
  
  private def calculateOrigin: Pixel = {
    lazy val anchors = unit.matchups.allies.filter(isAnchor)
    if (toReturn.isDefined) {
      toReturn.get
    }
    else if (toForm.isDefined) {
      toForm.get
    }
    else if (toLeash.isDefined) {
      toLeash.get.pixelCenter
    }
    else if (anchors.nonEmpty) {
      anchors.minBy(_.pixelDistanceEdge(unit)).pixelCenter
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
  
  private def isAnchor(ally: UnitInfo): Boolean = {
    if ( ! ally.unitClass.helpsInCombat) return false
    
    // Don't retreat to a Missile Turret against Zerglings, for example
    if (ally.matchups.enemies.nonEmpty && ally.matchups.targets.isEmpty) return false
    
    if (ally.topSpeed < unit.topSpeed && ally.unitClass.subjectiveValue > unit.unitClass.subjectiveValue) {
      return true
    }
    
    if ( ! ally.unitClass.canMove && ally.canAttack) {
      return true
    }
    
    false
  }
}
