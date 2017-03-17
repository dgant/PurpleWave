package Micro.Intentions

import Micro.Behaviors.Behavior
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
}
