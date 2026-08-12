package Micro.Agency

import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.UnitClasses.UnitClass

case class BuildIntent(unitClass: UnitClass, tile: Tile, startNow: Boolean = true, approach: Option[Pixel] = None)
