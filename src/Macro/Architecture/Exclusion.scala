package Macro.Architecture

import Macro.Architecture.PlacementRequests.PlacementRequest
import Mathematics.Points.TileRectangle

case class Exclusion(
  description: String,
  areaExcluded: TileRectangle,
  request: Option[PlacementRequest] = None)
