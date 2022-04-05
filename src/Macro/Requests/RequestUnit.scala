package Macro.Requests

import ProxyBwapi.UnitClasses._
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.TileFilters.{TileAny, TileFilter}

case class RequestUnit(
  unitClass       : UnitClass,
  quantityArg     : Int                       = 1,
  tileFilterArg   : TileFilter                = TileAny,
  specificUnitArg : Option[FriendlyUnitInfo]  = None) extends RequestBuildable(
    unitClass,
    quantityArg,
    tileFilterArg,
    specificUnitArg)