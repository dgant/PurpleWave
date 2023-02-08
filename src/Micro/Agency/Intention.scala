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
  var canSneak      : Boolean                       = false
  var canTickle     : Boolean                       = false
  var shouldMeld    : Boolean                       = false
  var shouldLiftoff : Boolean                       = false
  var targets       : Option[IndexedSet[UnitInfo]]  = None

  def setTravel         (value: Pixel)                    : Intention = { toTravel      = Some(value); this }
  def setReturnTo       (value: Pixel)                    : Intention = { toReturn      = Some(value); this }
  def setScan           (value: Pixel)                    : Intention = { toScan        = Some(value); this }
  def setNuke           (value: Pixel)                    : Intention = { toNuke        = Some(value); this }
  def setAttack         (value: UnitInfo)                 : Intention = { toAttack      = Some(value); this }
  def setGather         (value: UnitInfo)                 : Intention = { toGather      = Some(value); this }
  def setRepair         (value: UnitInfo)                 : Intention = { toRepair      = Some(value); this }
  def setFinish         (value: UnitInfo)                 : Intention = { toFinish      = Some(value); this }
  def setBoard          (value: FriendlyUnitInfo)         : Intention = { toBoard       = Some(value); this }
  def setTrain          (value: UnitClass)                : Intention = { toTrain       = Some(value); this }
  def setBuild          (value: UnitClass)                : Intention = { toBuild       = Some(value); this }
  def setBuildTile      (value: Tile)                     : Intention = { toBuildTile   = Some(value); this }
  def setUpgrade        (value: Upgrade)                  : Intention = { toUpgrade     = Some(value); this }
  def setTech           (value: Tech)                     : Intention = { toTech        = Some(value); this }
  def setTravel         (value: Option[Pixel])            : Intention = { toTravel      = value; this }
  def setReturnTo       (value: Option[Pixel])            : Intention = { toReturn      = value; this }
  def setScan           (value: Option[Pixel])            : Intention = { toScan        = value; this }
  def setNuke           (value: Option[Pixel])            : Intention = { toNuke        = value; this }
  def setAttack         (value: Option[UnitInfo])         : Intention = { toAttack      = value; this }
  def setGather         (value: Option[UnitInfo])         : Intention = { toGather      = value; this }
  def setRepair         (value: Option[UnitInfo])         : Intention = { toRepair      = value; this }
  def setFinish         (value: Option[UnitInfo])         : Intention = { toFinish      = value; this }
  def setBoard          (value: Option[FriendlyUnitInfo]) : Intention = { toBoard       = value; this }
  def setTrain          (value: Option[UnitClass])        : Intention = { toTrain       = value; this }
  def setBuild          (value: Option[UnitClass])        : Intention = { toBuild       = value; this }
  def setBuildTile      (value: Option[Tile])             : Intention = { toBuildTile   = value; this }
  def setUpgrade        (value: Option[Upgrade])          : Intention = { toUpgrade     = value; this }
  def setTech           (value: Option[Tech])             : Intention = { toTech        = value; this }
  def setAction         (value: Action)                   : Intention = { action        = value; this }
  def setScout          (value: Seq[Tile])                : Intention = { toScoutTiles  = value; this }
  def setCanFight       (value: Boolean = true)           : Intention = { canFight      = value; this }
  def setCanFlee        (value: Boolean = true)           : Intention = { canFlee       = value; this }
  def setCanSneak       (value: Boolean = true)           : Intention = { canSneak      = value; this }
  def setCanTickle      (value: Boolean = true)           : Intention = { canTickle     = value; this }
  def setShouldMeld     (value: Boolean = true)           : Intention = { shouldMeld    = value; this }
  def setShouldLiftoff  (value: Boolean = true)           : Intention = { shouldLiftoff = value; this }
  def setTargets        (value: Iterable[UnitInfo])       : Intention = { targets       = Some(new IndexedSet[UnitInfo](value)); this }
  def setTargets        (value: UnitInfo*)                : Intention = setTargets(value)

  // Ideally consistent with the Agent logic
  def destination: Option[Pixel] = toTravel
    .orElse(toBoard.map(_.pixel))
    .orElse(toAttack.orElse(toGather).orElse(toRepair).orElse(toFinish).map(_.pixel))
    .orElse(toNuke)
    .orElse(toBuildTile.map(_.center))
    .orElse(toScoutTiles.headOption.map(_.center))
    .orElse(toReturn)
}
