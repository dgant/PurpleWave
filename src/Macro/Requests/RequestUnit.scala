package Macro.Requests

import ProxyBwapi.UnitClasses._
import Utilities.TileFilters.{TileAny, TileFilter}

case class RequestUnit(unitClass: UnitClass, quantityArg: Int = 1, tileFilterArg: TileFilter = TileAny) extends RequestProduction(unitClass, quantityArg, tileFilterArg)