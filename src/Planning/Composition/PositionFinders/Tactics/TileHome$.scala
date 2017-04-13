package Planning.Composition.PixelFinders.Tactics

import Planning.Composition.PixelFinders.TileFinder
import Lifecycle.With
import Mathematics.Pixels.Tile

object TileHome$ extends TileFinder {
  
  override def find(): Option[Tile] = Some(With.geography.home)
}
