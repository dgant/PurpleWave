package Information.Grids.Abstract

import Geometry.Shapes.Circle
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.EnrichPosition._
import bwapi.TilePosition

abstract class GridVision extends GridBoolean {
  
  override def update(relevantTiles:Iterable[TilePosition]) {
    reset(relevantTiles)
    units
      .filter(_.possiblyStillThere)
      .foreach(u => {
      Circle.points(u.unitClass.sightRange / 32)
        .map(u.tileCenter.add)
        .foreach(set(_, true))
    })
  }
  
  protected def units:Iterable[UnitInfo]
}