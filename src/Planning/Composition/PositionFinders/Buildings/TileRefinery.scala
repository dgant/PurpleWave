package Planning.Composition.PositionFinders.Buildings

import Planning.Composition.PositionFinders.TileFinder
import Lifecycle.With
import bwapi.TilePosition


object TileRefinery extends TileFinder {
  
  def find: Option[TilePosition] = {
    
    val candidateAreas = With.geography.ourBases
      .toVector
      .sortBy( ! _.townHall.exists(_.complete))
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
