package Planning.Composition.PixelFinders.Generic

import Mathematics.Pixels.{Points, Tile}
import Planning.Composition.PixelFinders.TileFinder

object TileMiddle extends TileFinder {
  
  override def find(): Option[Tile] = Some(Points.tileMiddle)
}
