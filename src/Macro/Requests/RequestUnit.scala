package Macro.Requests

import Lifecycle.With
import Placement.Access.PlacementQuery
import ProxyBwapi.UnitClasses._
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

case class RequestUnit(
  unitClass         : UnitClass,
  quantityArg       : Int                       = 1,
  minStartFrameArg  : Int                       = 0,
  placementQueryArg : Option[PlacementQuery]    = None,
  specificUnitArg   : Option[FriendlyUnitInfo]  = None) extends RequestBuildable(
    unitClass,
    quantityArg,
    ?(minStartFrameArg <= With.frame, 0, minStartFrameArg), // Not strictly necessary but reduces bug surface area
    placementQueryArg,
    specificUnitArg)