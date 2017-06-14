package Planning.Composition.PixelFinders.Generic

import Mathematics.Points.{SpecificPoints, Tile}
import Planning.Composition.PixelFinders.TileFinder

object TileMiddle extends TileFinder {
  
  override def find(): Option[Tile] = Some(SpecificPoints.tileMiddle)
}
