package Planning.Composition.PositionFinders.Buildings

import Planning.Composition.PositionFinders.PositionFinder
import Startup.With
import bwapi.TilePosition


object PositionTownHall extends PositionFinder {
  
  def find: Option[TilePosition] = {
  
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
