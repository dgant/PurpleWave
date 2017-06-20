package Micro.Intent

import Mathematics.Points.{Pixel, Tile}
import Planning.Plan
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade

class Intention(val plan: Plan, val unit: FriendlyUnitInfo) {
  
  // Modify only on construction
  var toReturn    : Option[Pixel]     = None
  var toTravel    : Option[Pixel]     = None
  var toAttack    : Option[UnitInfo]  = None
  var toGather    : Option[UnitInfo]  = None
  var toBuild     : Option[UnitClass] = None
  var toBuildTile : Option[Tile]      = None
  var toTrain     : Option[UnitClass] = None
  var toTech      : Option[Tech]      = None
  var toForm      : Option[Pixel]     = None
  var toUpgrade   : Option[Upgrade]   = None
  var canAttack   : Boolean           = true
  var canFlee     : Boolean           = true
  var canCower    : Boolean           = true
}
