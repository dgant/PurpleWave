package Planning.Composition.PixelFinders

import Mathematics.Points.Tile

trait TileFinder {
  
  def find:Option[Tile]
  
}
