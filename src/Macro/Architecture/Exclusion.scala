package Macro.Architecture

import Mathematics.Points.TileRectangle

case class Exclusion(
  description     : String,
  areaExcluded    : TileRectangle,
  gasAllowed      : Boolean,
  townHallAllowed : Boolean)
