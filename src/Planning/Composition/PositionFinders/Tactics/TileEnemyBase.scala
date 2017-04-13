package Planning.Composition.PixelFinders.Tactics

import Planning.Composition.PixelFinders.TileFinder
import Lifecycle.With
import Mathematics.Pixels.Tile

class TileEnemyBase extends TileFinder {
  
  override def find: Option[Tile] = Some(With.intelligence.mostBaselikeEnemyPixel)
  
}
