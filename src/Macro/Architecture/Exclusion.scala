package Macro.Architecture

import Mathematics.Points.TileRectangle

case class Exclusion(
  description     : String,
  area            : TileRectangle,
  gasAllowed      : Boolean,
  townHallAllowed : Boolean)
