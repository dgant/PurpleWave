package Micro.Intent

import Lifecycle.With
import Micro.Behaviors.{MovementProfiles, TargetingProfiles}
import Micro.State.ExecutionState
import Performance.Caching.CacheForever
import Planning.Plan
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import bwapi.TilePosition

class Intention(val plan:Plan, val unit:FriendlyUnitInfo) {
  
  def state:ExecutionState = With.executor.getState(unit)
  
  var destination : Option[TilePosition]  = None
  var toAttack    : Option[UnitInfo]      = None
  var toGather    : Option[UnitInfo]      = None
  var toBuild     : Option[UnitClass]     = None
  var toTech      : Option[Tech]          = None
  var toUpgrade   : Option[Upgrade]       = None
  
  var leash         : Int     = Int.MaxValue
  var desireToFight : Double  = 1.0
  
  def targets: Set[UnitInfo] = targetCache.get
  def threats: Set[UnitInfo] = threatCache.get
 
  var movementProfile = MovementProfiles.defaultCombat
  var targetProfile         = TargetingProfiles.default
  
  private val targetCache = new CacheForever[Set[UnitInfo]](() => Targets.get(this))
  private val threatCache = new CacheForever[Set[UnitInfo]](() => Threats.get(this))
}
