package Macro.Architecture

import Mathematics.Points.Tile

case class Placement(
  buildingDescriptor  : BuildingDescriptor,
  tile                : Tile,
  createdFrame        : Int)
