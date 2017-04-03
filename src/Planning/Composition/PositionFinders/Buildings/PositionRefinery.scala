package Planning.Composition.PositionFinders.Buildings

import Planning.Composition.PositionFinders.PositionFinder
import Startup.With
import bwapi.TilePosition


object PositionRefinery extends PositionFinder {
  
  def find: Option[TilePosition] = {
    
    val candidateAreas = With.geography.ourBases
      .toList
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
