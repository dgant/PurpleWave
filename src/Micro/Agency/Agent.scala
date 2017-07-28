package Micro.Agency

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Micro.Actions.{Action, Idle}
import Micro.Decisions.MicroDecision
import Micro.Heuristics.Movement.{MovementHeuristicEvaluation, MovementProfile}
import Micro.Heuristics.Targeting.TargetingProfile
import Performance.Caching.CacheFrame
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
  
  /////////////
  // History //
  /////////////
  
  var intention: Intention = new Intention(With.strategy.gameplan)

  var lastAction: Option[Action] = None
  var lastFrame: Int = 0
  
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
  var shovers       : ListBuffer[FriendlyUnitInfo]  = new ListBuffer[FriendlyUnitInfo]
  
  var movementProfile : MovementProfile   = MovementProfiles.default
  var targetProfile   : TargetingProfile  = TargetingProfiles.default
  
  var explosions: ListBuffer[Explosion] = new ListBuffer[Explosion]
  
  /////////////////
  // Suggestions //
  /////////////////
  
  def origin: Pixel = toReturn.getOrElse(originCache.get)
  private val originCache = new CacheFrame(() => if (With.geography.ourBases.nonEmpty) With.geography.ourBases.map(_.heart.pixelCenter).minBy(unit.pixelDistanceTravelling) else With.geography.home.pixelCenter)
  
  /////////////////
  // Diagnostics //
  /////////////////
  
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
  
    ///////////
    // Setup //
    ///////////
  
    unit.agent.toReturn        = unit.agent.intention.toReturn
    unit.agent.toTravel        = unit.agent.intention.toTravel
    unit.agent.toAttack        = unit.agent.intention.toAttack
    unit.agent.toGather        = unit.agent.intention.toGather
    unit.agent.toBuild         = unit.agent.intention.toBuild
    unit.agent.toBuildTile     = unit.agent.intention.toBuildTile
    unit.agent.toTrain         = unit.agent.intention.toTrain
    unit.agent.toTech          = unit.agent.intention.toTech
    unit.agent.toUpgrade       = unit.agent.intention.toUpgrade
    unit.agent.toForm          = unit.agent.intention.toForm
    unit.agent.canFight        = unit.agent.intention.canAttack
    unit.agent.canFlee         = unit.agent.intention.canFlee
    unit.agent.canPursue       = unit.agent.intention.canPursue
    unit.agent.canCower        = unit.agent.intention.canCower
    unit.agent.canMeld         = unit.agent.intention.canMeld
    unit.agent.canScout        = unit.agent.intention.canScout
  
    unit.agent.movementProfile = MovementProfiles.default
  
    /////////////////
    // Diagnostics //
    /////////////////
  
    unit.agent.desireTeam        = 1.0
    unit.agent.desireIndividual  = 1.0
    unit.agent.desireTotal       = 1.0
  
    /////////
    // Go! //
    /////////
    
    Idle.consider(unit)
  
    unit.agent.shovers.clear()
  }
}
