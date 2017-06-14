package Micro.Task

import Information.Battles.Estimation.BattleEstimation
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Micro.Actions.Action
import Micro.Behaviors.{MovementProfiles, TargetingProfiles}
import Micro.Heuristics.Movement.{MovementHeuristicResult, MovementProfile}
import Micro.Heuristics.Targeting.{EvaluateTargets, TargetHeuristicResult, TargetingProfile}
import Micro.Intent.{Intention, Targets, Threats}
import Performance.Caching.CacheFrame
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade

class ExecutionState(val unit: FriendlyUnitInfo) {
  
  /////////////
  // History //
  /////////////
  
  var intent: Intention = new Intention(With.gameplan, unit)

  var lastAction: Option[Action] = None
  var lastFrame: Int = 0
  
  ///////////////
  // Decisions //
  ///////////////
  
  var toReturn      : Option[Pixel]     = None
  var toTravel      : Option[Pixel]     = None
  var toAttack      : Option[UnitInfo]  = None
  var toGather      : Option[UnitInfo]  = None
  var toBuild       : Option[UnitClass] = None
  var toBuildTile   : Option[Tile]      = None
  var toTrain       : Option[UnitClass] = None
  var toTech        : Option[Tech]      = None
  var toForm        : Option[Pixel]     = None
  var toUpgrade     : Option[Upgrade]   = None
  var canPursue     : Boolean           = true
  var canAttack     : Boolean           = true
  
  var movementProfile : MovementProfile   = MovementProfiles.default
  var targetProfile   : TargetingProfile  = TargetingProfiles.default
  
  /////////////////
  // Suggestions //
  /////////////////
  
  def battleEstimation:Option[BattleEstimation] = With.battles.byUnit.get(unit).map(_.estimation)
  
  def origin: Pixel = toReturn.getOrElse(originCache.get)
  private val originCache = new CacheFrame(() => if (With.geography.ourBases.nonEmpty) With.geography.ourBases.map(_.heart.pixelCenter).minBy(unit.pixelDistanceTravelling) else With.geography.home.pixelCenter)
  
  def threats         : Vector[UnitInfo]      = threatsCache.get
  def threatsActive   : Vector[UnitInfo]      = threatsActiveCache.get
  def targets         : Vector[UnitInfo]      = targetsCache.get
  def targetsInRange  : Vector[UnitInfo]      = targetsInRangeCache.get
  def targetValues    : Map[UnitInfo, Double] = targetValuesCache.get
  private val threatsCache        = new CacheFrame(() => Threats.get(intent))
  private val threatsActiveCache  = new CacheFrame(() => threats.filter(_.isBeingViolentTo(unit)))
  private val targetsCache        = new CacheFrame(() => Targets.get(intent))
  private val targetsInRangeCache = new CacheFrame(() => targets.filter(target => Targets.inRange(intent, target)))
          val targetValuesCache   = new CacheFrame(() => targets.map(target => (target, EvaluateTargets.evaluate(this, target))).toMap)
  
  /////////////////
  // Diagnostics //
  /////////////////
  
  var movingTo                  : Option[Pixel] = None
  var movedHeuristicallyFrame   : Int = 0
  var movementHeuristicResults  : Iterable[MovementHeuristicResult] = Vector.empty
  
  var target                      : Option[UnitInfo] = None
  var targetedHeuristicallyFrame  : Int = 0
  var targetHeuristicResults      : Iterable[TargetHeuristicResult]   = Vector.empty
}
