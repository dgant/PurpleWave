package Micro.Intentions

import Micro.Behaviors.{Behavior, MovementProfiles, TargetingProfiles}
import Micro.Targeting.Targets
import Planning.Plan
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import bwapi.TilePosition

class Intention(val plan:Plan, val unit:FriendlyUnitInfo) {
  
  var behavior    : Behavior              = unit.unitClass.behavior
  var destination : Option[TilePosition]  = None
  var toGather    : Option[UnitInfo]      = None
  var toBuild     : Option[UnitClass]     = None
  var toTech      : Option[Tech]          = None
  var toUpgrade   : Option[Upgrade]       = None
  
  private var targetCache: Option[Set[UnitInfo]] = None
  def targets: Set[UnitInfo] = {
    if (targetCache == None) targetCache = Some(Targets.get(this))
    targetCache.get
  }
 
  var movementProfileCombat = MovementProfiles.defaultCombat
  var movementProfileNormal = MovementProfiles.defaultNormal
  var targetProfile = TargetingProfiles.default
}
