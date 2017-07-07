package Micro.Execution

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Micro.Actions.Action
import Micro.Behaviors.{MovementProfiles, TargetingProfiles}
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
  
  var intent: Intention = new Intention(With.strategy.gameplan, unit)

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
  var canPursue     : Boolean                       = true
  var canCower      : Boolean                       = false
  var shovers       : ListBuffer[FriendlyUnitInfo]  = new ListBuffer[FriendlyUnitInfo]
  
  var movementProfile : MovementProfile   = MovementProfiles.default
  var targetProfile   : TargetingProfile  = TargetingProfiles.default
  
  var explosions: ListBuffer[Explosion] = new ListBuffer[Explosion]
  
  /////////////////
  // Suggestions //
  /////////////////
  
  def origin: Pixel = toReturn.getOrElse(originCache.get)
  private val originCache = new CacheFrame(() => if (With.geography.ourBases.nonEmpty) With.geography.ourBases.map(_.heart.pixelCenter).minBy(unit.pixelDistanceTravelling) else With.geography.home.pixelCenter)
  
  def neighbors       : Vector[FriendlyUnitInfo]  = neighborsCache.get
  def threats         : Vector[UnitInfo]          = threatsCache.get
  def threatsViolent  : Vector[UnitInfo]          = threatsViolentCache.get
  def targets         : Vector[UnitInfo]          = targetsCache.get
  def targetsInRange  : Vector[UnitInfo]          = targetsInRangeCache.get
  def targetValues    : Map[UnitInfo, Double]     = targetValuesCache.get
  
  private val neighborsCache      = new CacheFrame(() => Neighbors.get(this))
  private val threatsCache        = new CacheFrame(() => Threats.get(this))
  private val threatsViolentCache = new CacheFrame(() => threats.filter(_.isBeingViolentTo(unit)))
  private val targetsCache        = new CacheFrame(() => Targets.get(this))
  private val targetsInRangeCache = new CacheFrame(() => targets.filter(target => Targets.inRange(this, target)))
          val targetValuesCache   = new CacheFrame(() => targets.map(target => (target, EvaluateTargets.evaluate(this, target))).toMap)
  
  /////////////////
  // Diagnostics //
  /////////////////
  
  var movingTo                  : Option[Pixel] = None
  var movedHeuristicallyFrame   : Int = 0
  var movementHeuristicResults  : Iterable[MovementHeuristicEvaluation] = Iterable.empty
}
