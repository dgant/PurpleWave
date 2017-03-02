package Geometry.Field

import Geometry.Circle
import Startup.With
import bwapi.TilePosition
import Utilities.Enrichment.EnrichPosition._

class MapMobility extends InfluenceMap {
  
  var _initialized = false
  override def update() {
    if (_initialized) return
    
    With.maps.walkability.update()
        
    points
      .map(point => new TilePosition(point._1, point._2))
      .filter(With.maps.walkability.get(_) > 0)
      .foreach(ourPosition =>
        Circle.points(3)
          .map(delta => ourPosition.add(delta._1, delta._2))
          .foreach(nearbyPosition => add(ourPosition, With.maps.walkability.get(nearbyPosition))))
        
    _initialized = true
  }
}
