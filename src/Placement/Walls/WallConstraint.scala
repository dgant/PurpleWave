package Placement.Walls

import Placement.Walls.WallSpans.WallSpan
import ProxyBwapi.UnitClasses.UnitClass

case class WallConstraint(
  gapTiles    : Int,
  blocksUnit  : UnitClass,
  span        : WallSpan,
  buildings   : UnitClass*)