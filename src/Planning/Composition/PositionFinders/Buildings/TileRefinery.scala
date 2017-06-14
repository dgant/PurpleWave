package Planning.Composition.PixelFinders.Buildings

import Planning.Composition.PixelFinders.TileFinder
import Lifecycle.With
import Mathematics.Points.Tile


object TileRefinery extends TileFinder {
  
  def find: Option[Tile] = {
    
    val candidateAreas = With.geography.ourBases
      .toVector
      .sortBy( ! _.townHall.exists(_.aliveAndComplete))
      .sortBy( - _.gasLeft)
      .view
      .flatten(_.gas)
      .filter(_.isNeutral)
      .map(_.tileArea)
    
    candidateAreas
      .filter(With.realEstate.available)
      .map(_.startInclusive)
      .headOption
  }
  
}
