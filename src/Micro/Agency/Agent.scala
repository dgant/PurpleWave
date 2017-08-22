package Micro.Agency

import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.{Pixel, PixelRay, Tile}
import Micro.Actions.{Action, Idle}
import Micro.Decisions.MicroDecision
import Micro.Heuristics.Targeting.TargetingProfile
import Performance.Caching.CacheFrame
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
  var desireTotal       : Double = _
  
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
    unit.agent.desireTeam         = 1.0
    unit.agent.desireIndividual   = 1.0
    unit.agent.desireTotal        = 1.0
    unit.agent.targetingProfile   = TargetingProfiles.default
    unit.agent.pathsAll           = Seq.empty
    unit.agent.pathsTruncated     = Seq.empty
    unit.agent.pathsAcceptable    = Seq.empty
    unit.agent.pathAccepted       = Seq.empty
  }
  
  private def followIntent() {
    val intent = unit.agent.lastIntent
    unit.agent.toReturn     = intent.toReturn
    unit.agent.toTravel     = intent.toTravel
    unit.agent.toAttack     = intent.toAttack
    unit.agent.toScan       = intent.toScan
    unit.agent.toGather     = intent.toGather
    unit.agent.toAddon      = intent.toAddon
    unit.agent.toBuild      = intent.toBuild
    unit.agent.toBuildTile  = intent.toBuildTile
    unit.agent.toTrain      = intent.toTrain
    unit.agent.toTech       = intent.toTech
    unit.agent.toFinish     = intent.toFinish
    unit.agent.toUpgrade    = intent.toUpgrade
    unit.agent.toForm       = intent.toForm
    unit.agent.toBoard      = None
    unit.agent.canFight     = intent.canAttack
    unit.agent.canFlee      = intent.canFlee
    unit.agent.canCower     = intent.canCower
    unit.agent.canMeld      = intent.canMeld
    unit.agent.canScout     = intent.canScout
    unit.agent.canPillage   = intent.canPillage
  }
  
  private def cleanUp() {
    unit.agent.shovers.clear()
    explosions.clear()
  }
  
  private def calculateOrigin: Pixel = {
    lazy val anchors = unit.matchups.allies.filter(isAnchor)
    if (unit.agent.toReturn.isDefined) {
      unit.agent.toReturn.get
    }
    else if (unit.agent.toForm.isDefined) {
      unit.agent.toForm.get
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
    
    if (ally.topSpeed < unit.topSpeed && ally.subjectiveValue > unit.subjectiveValue) {
      return true
    }
    
    if ( ! ally.unitClass.canMove && ally.canAttack) {
      return true
    }
    
    false
  }
}
