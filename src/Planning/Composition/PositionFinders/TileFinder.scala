package Planning.Composition.PixelFinders

import Mathematics.Pixels.Tile

trait TileFinder {
  
  def find:Option[Tile]
  
}
