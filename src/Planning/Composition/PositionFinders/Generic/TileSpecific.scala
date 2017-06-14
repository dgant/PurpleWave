package Planning.Composition.PixelFinders.Generic

import Mathematics.Points.Tile
import Planning.Composition.PixelFinders.TileFinder

case class TileSpecific(val position:Tile) extends TileFinder {
  override def find(): Option[Tile] = Some(position)
}
