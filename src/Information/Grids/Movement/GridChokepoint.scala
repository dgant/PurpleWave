package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With
import Mathematics.Points.Tile

class GridChokepoint extends AbstractGridBoolean {
  override def onInitialization() {
    val edges = With.geography.zones.flatten(_.edges)
    
    indices.foreach(i => {
      val center = new Tile(i).pixelCenter
      set(i, edges.exists(edge => edge.pixelCenter.pixelDistanceFast(center) <= edge.radiusPixels + 64.0))
    })
  }
}
