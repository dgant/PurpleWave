package Micro.State

import Information.Battles.Estimation.BattleEstimationResult
import Lifecycle.With
import Mathematics.Pixels.{Pixel, Tile}
import Micro.Actions.Action
import Micro.Behaviors.{MovementProfiles, TargetingProfiles}
import Micro.Heuristics.Movement.{MovementHeuristicResult, MovementProfile}
import Micro.Heuristics.Targeting.{TargetHeuristicResult, TargetingProfile}
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
  
  ///////////////
  // Decisions //
  ///////////////
  
  var toTravel    : Option[Pixel]     = None
  var toAttack    : Option[UnitInfo]  = None
  var toGather    : Option[UnitInfo]  = None
  var toBuild     : Option[UnitClass] = None
  var toBuildTile : Option[Tile]      = None
  var toTrain     : Option[UnitClass] = None
  var toTech      : Option[Tech]      = None
  var toUpgrade   : Option[Upgrade]   = None
  var canAttack   : Boolean           = true
  var canPursue   : Boolean           = true
  
  var movementProfile : MovementProfile   = MovementProfiles.default
  var targetProfile   : TargetingProfile  = TargetingProfiles.default
  /////////////////
  // Suggestions //
  /////////////////
  
  def battleEstimation:Option[BattleEstimationResult] = With.battles.byUnit.get(unit).map(_.estimation).flatten
  
  def origin: Pixel = originCache.get
  private val originCache = new CacheFrame(() => if (With.geography.ourBases.nonEmpty) With.geography.ourBases.map(_.heart.pixelCenter).minBy(unit.pixelDistanceTravelling) else With.geography.home.pixelCenter)
  
  def targets        = targetsCache.get
  def targetsInRange = targetsInRangeCache.get
  def threats        = threatsCache.get
  def threatsActive  = threatsActiveCache.get
  private val targetsCache        = new CacheFrame(() => Targets.get(intent))
  private val targetsInRangeCache = new CacheFrame(() => targets.filter(target => Targets.inRange(intent, target)))
  private val threatsCache        = new CacheFrame(() => Threats.get(intent))
  private val threatsActiveCache  = new CacheFrame(() => threats.filter(threat => Threats.active(intent, threat)))
  

  
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
