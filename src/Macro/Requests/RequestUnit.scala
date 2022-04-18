package Macro.Requests

import Placement.Access.PlacementQuery
import ProxyBwapi.UnitClasses._
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class RequestUnit(
  unitClass         : UnitClass,
  quantityArg       : Int                       = 1,
  placementQueryArg : Option[PlacementQuery]    = None,
  specificUnitArg   : Option[FriendlyUnitInfo]  = None) extends RequestBuildable(
    unitClass,
    quantityArg,
    placementQueryArg,
    specificUnitArg)