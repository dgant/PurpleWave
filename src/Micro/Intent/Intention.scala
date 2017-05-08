package Micro.Intent

import Information.Battles.TacticsTypes.TacticsOptions
import Lifecycle.With
import Mathematics.Pixels.{Pixel, Tile}
import Micro.Behaviors.{MovementProfiles, TargetingProfiles}
import Micro.State.ExecutionState
import Performance.Caching.CacheFrame
import Planning.Plan
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade

class Intention(val plan:Plan, val unit:FriendlyUnitInfo) {
  
  def state:ExecutionState = unit.executionState
  def tactics:Option[TacticsOptions] = With.battles.byUnit.get(unit).map(b => b.bestTactics)
  
  var origin      : Pixel             = if (With.geography.ourBases.nonEmpty) With.geography.ourBases.map(_.heart.pixelCenter).minBy(unit.pixelDistanceTravelling) else With.geography.home.pixelCenter
  var destination : Option[Pixel]     = None
  var toAttack    : Option[UnitInfo]  = None
  var toGather    : Option[UnitInfo]  = None
  var toBuild     : Option[UnitClass] = None
  var toBuildTile : Option[Tile]      = None
  var toTrain     : Option[UnitClass] = None
  var toTech      : Option[Tech]      = None
  var toUpgrade   : Option[Upgrade]   = None
  var leash       : Option[Int]       = None
  var canAttack   : Boolean           = true
  var canPursue   : Boolean           = false
  
  def targets        = targetsCache.get
  def targetsInRange = targetsInRangeCache.get
  def threats        = threatsCache.get
  def threatsActive  = threatsActiveCache.get
  private val targetsCache        = new CacheFrame(() => Targets.get(this))
  private val targetsInRangeCache = new CacheFrame(() => targets.filter(target => Targets.inRange(this, target)))
  private val threatsCache        = new CacheFrame(() => Threats.get(this))
  private val threatsActiveCache  = new CacheFrame(() => threats.filter(threat => Threats.active(this, threat)))
 
  var movementProfile = MovementProfiles.default
  var targetProfile   = TargetingProfiles.default
}
