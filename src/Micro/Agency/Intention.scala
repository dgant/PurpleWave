package Micro.Agency

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Micro.Actions.{Action, Idle}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.UnitTracking.IndexedSet
import ProxyBwapi.Upgrades.Upgrade

class Intention {
  val frameCreated  : Int                           = With.frame
  var toTravel      : Option[Pixel]                 = None
  var toReturn      : Option[Pixel]                 = None
  var toScan        : Option[Pixel]                 = None
  var toNuke        : Option[Pixel]                 = None
  var toAttack      : Option[UnitInfo]              = None
  var toGather      : Option[UnitInfo]              = None
  var toRepair      : Option[UnitInfo]              = None
  var toFinish      : Option[UnitInfo]              = None
  var toBoard       : Option[FriendlyUnitInfo]      = None
  var toTrain       : Option[UnitClass]             = None
  var toBuild       : Option[UnitClass]             = None
  var toBuildTile   : Option[Tile]                  = None
  var toTech        : Option[Tech]                  = None
  var toUpgrade     : Option[Upgrade]               = None
  var toScoutTiles  : Seq[Tile]                     = Seq.empty
  var action        : Action                        = Idle
  var canFight      : Boolean                       = true
  var canFlee       : Boolean                       = true
  var canTickle     : Boolean                       = false
  var shouldMeld    : Boolean                       = false
  var shouldLiftoff : Boolean                       = false
  var targets       : Option[IndexedSet[UnitInfo]]  = None

  def setTravel         (value: Pixel)              : Intention = { toTravel      = Some(value); this }
  def setReturnTo       (value: Pixel)              : Intention = { toReturn      = Some(value); this }
  def setScan           (value: Pixel)              : Intention = { toScan        = Some(value); this }
  def setNuke           (value: Pixel)              : Intention = { toNuke        = Some(value); this }
  def setAttack         (value: UnitInfo)           : Intention = { toAttack      = Some(value); this }
  def setGather         (value: UnitInfo)           : Intention = { toGather      = Some(value); this }
  def setRepair         (value: UnitInfo)           : Intention = { toRepair      = Some(value); this }
  def setFinish         (value: UnitInfo)           : Intention = { toFinish      = Some(value); this }
  def setBoard          (value: FriendlyUnitInfo)   : Intention = { toBoard       = Some(value); this }
  def setTrain          (value: UnitClass)          : Intention = { toTrain       = Some(value); this }
  def setBuild          (value: UnitClass)          : Intention = { toBuild       = Some(value); this }
  def setBuildTile      (value: Tile)               : Intention = { toBuildTile   = Some(value); this }
  def setUpgrade        (value: Upgrade)            : Intention = { toUpgrade     = Some(value); this }
  def setTech           (value: Tech)               : Intention = { toTech        = Some(value); this }
  def setAction         (value: Action)             : Intention = { action        = value; this }
  def setScout          (value: Seq[Tile])          : Intention = { toScoutTiles  = value; this }
  def setCanFight       (value: Boolean)            : Intention = { canFight      = value; this }
  def setCanFlee        (value: Boolean)            : Intention = { canFlee       = value; this }
  def setCanTickle      (value: Boolean)            : Intention = { canTickle     = value; this }
  def setShouldMeld     (value: Boolean)            : Intention = { shouldMeld    = value; this }
  def setShouldLiftoff  (value: Boolean)            : Intention = { shouldLiftoff = value; this }
  def setTargets        (value: UnitInfo*)          : Intention = setTargets(value: _*)
  def setTargets        (value: Iterable[UnitInfo]) : Intention = { targets       = Some(new IndexedSet[UnitInfo](value)); this }
}
