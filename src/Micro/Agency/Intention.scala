package Micro.Agency

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade

class Intention {
  val frameCreated         = With.frame
  var toReturn    : Option[Pixel]             = None
  var toTravel    : Option[Pixel]             = None
  var toAttack    : Option[UnitInfo]          = None
  var toScan      : Option[Pixel]             = None
  var toGather    : Option[UnitInfo]          = None
  var toAddon     : Option[UnitClass]         = None
  var toBuild     : Option[UnitClass]         = None
  var toBuildTile : Option[Tile]              = None
  var toTrain     : Option[UnitClass]         = None
  var toTech      : Option[Tech]              = None
  var toFinish    : Option[UnitInfo]          = None
  var toForm      : Option[Pixel]             = None
  var toNuke      : Option[Pixel]             = None
  var toUpgrade   : Option[Upgrade]           = None
  var toLeash     : Option[Leash]             = None
  var toRepair    : Option[UnitInfo]          = None
  var toBoard     : Option[FriendlyUnitInfo]  = None
  var canAttack   : Boolean                   = true
  var canFlee     : Boolean                   = true
  var canMeld     : Boolean                   = false
  var canTickle   : Boolean                   = false
  var canScout    : Boolean                   = false
  var canLiftoff  : Boolean                   = false
  var canCancel   : Boolean                   = false
  var canFocus    : Boolean                   = false
}
