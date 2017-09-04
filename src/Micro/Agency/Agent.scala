package Micro.Agency

import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.{Pixel, PixelRay, Tile}
import Micro.Actions.{Action, Idle}
import Micro.Decisions.MicroDecision
import Micro.Heuristics.Targeting.TargetingProfile
import Performance.CacheFrame
import Planning.Plan
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import bwapi.Color

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

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
  var toBoard       : Option[FriendlyUnitInfo]      = None
  var canFight      : Boolean                       = true
  var canFlee       : Boolean                       = true
  var canCower      : Boolean                       = false
  var canMeld       : Boolean                       = false
  var canScout      : Boolean                       = false
  var canPillage    : Boolean                       = false
  var canBerzerk    : Boolean                       = false
  
  var targetingProfile: TargetingProfile = TargetingProfiles.default
  
  var explosions: ListBuffer[Explosion] = new ListBuffer[Explosion]
  
  var lastStim: Int = 0
  var lastCloak: Int = 0
  var shouldEngage: Boolean = false
  val forces: mutable.Map[Color, Force] = new mutable.HashMap[Color, Force]
  
  
  /////////////////
  // Suggestions //
  /////////////////
  
  def origin: Pixel = toReturn.getOrElse(originCache.get)
  private val originCache = new CacheFrame(() => calculateOrigin)
  
  /////////////////
  // Diagnostics //
  /////////////////
  
  var lastFrame   : Int             = 0
  var lastClient  : Option[Plan]    = None
  var lastAction  : Option[Action]  = None
  
  var desireTeam        : Double = _
  var desireIndividual  : Double = _
  
  var movingTo: Option[Pixel] = None
  
  var microDecisions              : Vector[MicroDecision] = Vector.empty
  var microDecisionsUpdateFrame   : Int = 0
  
  var pathsAll        : Traversable[PixelRay] = Seq.empty
  var pathsTruncated  : Traversable[PixelRay] = Seq.empty
  var pathsAcceptable : Traversable[PixelRay] = Seq.empty
  var pathAccepted    : Traversable[PixelRay] = Seq.empty
  
  ///////////////
  // Execution //
  ///////////////
  
  def execute() {
    resetState()
    followIntent()
    Idle.consider(unit)
    cleanUp()
  }
  
  private def resetState() {
    forces.clear()
    explosions.clear()
    desireTeam          = 1.0
    desireIndividual    = 1.0
    movingTo            = None
    targetingProfile    = TargetingProfiles.default
    pathsAll            = Seq.empty
    pathsTruncated      = Seq.empty
    pathsAcceptable     = Seq.empty
    pathAccepted        = Seq.empty
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
    toForm        = intent.toForm
    toBoard       = None
    canFight      = intent.canAttack
    canFlee       = intent.canFlee
    canCower      = intent.canCower
    canMeld       = intent.canMeld
    canScout      = intent.canScout
    canPillage    = intent.canPillage
    canBerzerk    = intent.canBerzerk
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
    else if (anchors.nonEmpty) {
      anchors.minBy(_.pixelDistanceFast(unit)).pixelCenter
    }
    else if (With.geography.ourBases.nonEmpty) {
      With.geography.ourBases.map(_.heart.pixelCenter).minBy(unit.pixelDistanceTravelling)
    }
    else {
      With.geography.home.pixelCenter
    }
  }
  
  private def isAnchor(ally: UnitInfo): Boolean = {
    if ( ! ally.unitClass.helpsInCombat) return false
    
    // Don't retreat to a Missile Turret against Zerglings, for example
    if (ally.matchups.enemies.nonEmpty && ally.matchups.targets.isEmpty) return false
    
    if (ally.topSpeed < unit.topSpeed && ally.subjectiveValue > unit.subjectiveValue) {
      return true
    }
    
    if ( ! ally.unitClass.canMove && ally.canAttack) {
      return true
    }
    
    false
  }
}
