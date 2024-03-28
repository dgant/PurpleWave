package Micro.Agency

import Mathematics.Points.Tile
import ProxyBwapi.UnitClasses.UnitClass

case class BuildIntent(unitClass: UnitClass, tile: Tile, startNow: Boolean = true)