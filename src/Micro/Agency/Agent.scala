package Micro.Agency

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Micro.Actions.{Action, Idle}
import Micro.Decisions.MicroDecision
import Micro.Heuristics.Movement.{MovementHeuristicEvaluation, MovementProfile}
import Micro.Heuristics.Targeting.TargetingProfile
import Performance.Caching.CacheFrame
import Planning.Plan
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade

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
  var toGather      : Option[UnitInfo]              = None
  var toBuild       : Option[UnitClass]             = None
  var toBuildTile   : Option[Tile]                  = None
  var toTrain       : Option[UnitClass]             = None
  var toTech        : Option[Tech]                  = None
  var toForm        : Option[Pixel]                 = None
  var toUpgrade     : Option[Upgrade]               = None
  var canFight      : Boolean                       = true
  var canFlee       : Boolean                       = true
  var canPursue     : Boolean                       = true
  var canCower      : Boolean                       = false
  var canMeld       : Boolean                       = false
  var canScout      : Boolean                       = false
  
  var movementProfile   : MovementProfile   = MovementProfiles.default
  var targetingProfile  : TargetingProfile  = TargetingProfiles.default
  
  var explosions: ListBuffer[Explosion] = new ListBuffer[Explosion]
  
  /////////////////
  // Suggestions //
  /////////////////
  
  def origin: Pixel = toReturn.getOrElse(originCache.get)
  private val originCache = new CacheFrame(() => if (With.geography.ourBases.nonEmpty) With.geography.ourBases.map(_.heart.pixelCenter).minBy(unit.pixelDistanceTravelling) else With.geography.home.pixelCenter)
  
  /////////////////
  // Diagnostics //
  /////////////////
  
  var lastFrame   : Int             = 0
  var lastClient  : Option[Plan]    = None
  var lastAction  : Option[Action]  = None
  
  var desireTeam        : Double = _
  var desireIndividual  : Double = _
  var desireTotal       : Double = _
  
  var movingTo                    : Option[Pixel] = None
  var movedHeuristicallyFrame     : Int = 0
  var movementHeuristicResults    : Iterable[MovementHeuristicEvaluation] = Iterable.empty
  
  var microDecisions              : Vector[MicroDecision] = Vector.empty
  var microDecisionsUpdateFrame   : Int = 0
  
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
    unit.agent.desireTeam         = 1.0
    unit.agent.desireIndividual   = 1.0
    unit.agent.desireTotal        = 1.0
    unit.agent.movementProfile    = MovementProfiles.default
    unit.agent.targetingProfile   = TargetingProfiles.default
  }
  
  private def followIntent() {
    unit.agent.toReturn = unit.agent.lastIntent.toReturn
    unit.agent.toTravel = unit.agent.lastIntent.toTravel
    unit.agent.toAttack = unit.agent.lastIntent.toAttack
    unit.agent.toGather = unit.agent.lastIntent.toGather
    unit.agent.toBuild = unit.agent.lastIntent.toBuild
    unit.agent.toBuildTile = unit.agent.lastIntent.toBuildTile
    unit.agent.toTrain = unit.agent.lastIntent.toTrain
    unit.agent.toTech = unit.agent.lastIntent.toTech
    unit.agent.toUpgrade = unit.agent.lastIntent.toUpgrade
    unit.agent.toForm = unit.agent.lastIntent.toForm
    unit.agent.canFight = unit.agent.lastIntent.canAttack
    unit.agent.canFlee = unit.agent.lastIntent.canFlee
    unit.agent.canPursue = unit.agent.lastIntent.canPursue
    unit.agent.canCower = unit.agent.lastIntent.canCower
    unit.agent.canMeld = unit.agent.lastIntent.canMeld
    unit.agent.canScout = unit.agent.lastIntent.canScout
  }
  
  private def cleanUp() {
    unit.agent.shovers.clear()
    explosions.clear()
  }
}
