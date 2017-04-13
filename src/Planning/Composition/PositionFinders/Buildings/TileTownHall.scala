package Planning.Composition.PixelFinders.Buildings

import Planning.Composition.PixelFinders.TileFinder
import Lifecycle.With
import Mathematics.Pixels.Tile


object TileTownHall extends TileFinder {
  
  def find: Option[Tile] = {
  
    val candidates = With.geography.bases
      .filter(base =>
        ! base.zone.island &&
        base.townHall.isEmpty &&
        (base.gas.nonEmpty || With.geography.ourBases.flatten(_.gas).size >= 2))
      .map(base => base.townHallArea)
      .filter(townHallArea =>
        townHallArea.tiles.forall(With.grids.buildable.get) &&
        With.realEstate.available(townHallArea))
      .map(_.startInclusive)
  
    if (candidates.isEmpty) return None
    
    Some(candidates
      .minBy(candidate =>
        1.0 * With.geography.ourBases
          .map(base => With.paths.groundPixels(base.townHallArea.midpoint, candidate))
          .sum
          -
        0.75 * With.geography.enemyBases
          .map(base => With.paths.groundPixels(base.townHallArea.midpoint, candidate))
          .sum
      ))
  }
  
}
