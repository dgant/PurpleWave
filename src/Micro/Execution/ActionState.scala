package Micro.Execution

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Micro.Actions.Action
import Micro.Behaviors.{MovementProfiles, TargetingProfiles}
import Micro.Decisions.MicroDecision
import Micro.Heuristics.Movement.{MovementHeuristicEvaluation, MovementProfile}
import Micro.Heuristics.Targeting.{EvaluateTargets, TargetingProfile}
import Micro.Intent.Intention
import Performance.Caching.CacheFrame
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade

import scala.collection.mutable.ListBuffer

class ActionState(val unit: FriendlyUnitInfo) {
  
  ///////////////
  // Messaging //
  ///////////////
  
  def shove(shover: FriendlyUnitInfo) {
    shovers.append(shover)
  }
  
  /////////////
  // History //
  /////////////
  
  var intent: Intention = new Intention(With.strategy.gameplan)
  intent.unit = unit

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
  var shovers       : ListBuffer[FriendlyUnitInfo]  = new ListBuffer[FriendlyUnitInfo]
  
  var movementProfile : MovementProfile   = MovementProfiles.default
  var targetProfile   : TargetingProfile  = TargetingProfiles.default
  
  var explosions: ListBuffer[Explosion] = new ListBuffer[Explosion]
  
  /////////////////
  // Suggestions //
  /////////////////
  
  def origin: Pixel = toReturn.getOrElse(originCache.get)
  private val originCache = new CacheFrame(() => if (With.geography.ourBases.nonEmpty) With.geography.ourBases.map(_.heart.pixelCenter).minBy(unit.pixelDistanceTravelling) else With.geography.home.pixelCenter)
  
  def targetValues: Map[UnitInfo, Double] = targetValuesCache.get
  val targetValuesCache   = new CacheFrame(() => unit.matchups.targets.map(target => (target, EvaluateTargets.evaluate(this, target))).toMap)
  
  //////////
  // Mood //
  //////////
  
  var desireTeam        : Double = 0.0
  var desireIndividual  : Double = 0.0
  var desireTotal       : Double = 0.0
  
  /////////////////
  // Diagnostics //
  /////////////////
  
  var movingTo                    : Option[Pixel] = None
  var movedHeuristicallyFrame     : Int = 0
  var movementHeuristicResults    : Iterable[MovementHeuristicEvaluation] = Iterable.empty
  
  var microDecisions              : Vector[MicroDecision] = Vector.empty
  var microDecisionsUpdateFrame   : Int = 0
}
