package Placement.Architecture

import Mathematics.Points.TileRectangle

case class Exclusion(description: String, areaExcluded: TileRectangle, request: Option[BuildingPlacement] = None)
