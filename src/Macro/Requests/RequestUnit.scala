package Macro.Requests

import Placement.Access.PlacementQuery
import ProxyBwapi.UnitClasses._
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class RequestUnit(
  unitClass         : UnitClass,
  quantityArg       : Int                       = 1,
  minStartFrameArg  : Int                       = 0,
  placementQueryArg : Option[PlacementQuery]    = None,
  specificUnitArg   : Option[FriendlyUnitInfo]  = None) extends RequestBuildable(
    unitClass,
    quantityArg,
    minStartFrameArg,
    placementQueryArg,
    specificUnitArg)