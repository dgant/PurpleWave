package Macro.Architecture.Tiles

import Macro.Architecture.Blueprint
import Mathematics.Points.Tile

trait TileSource {
  
  def appropriateFor(blueprint: Blueprint): Boolean
  
  def tiles(blueprint: Blueprint): Iterable[Tile]
}
