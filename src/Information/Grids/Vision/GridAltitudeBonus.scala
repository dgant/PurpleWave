package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridDouble
import Lifecycle.With
import Mathematics.Points.Tile

class GridAltitudeBonus extends AbstractGridDouble {
  
  override def onInitialization() {
    indices.foreach(i => set(i, getBonus(With.game.getGroundHeight(new Tile(i).bwapi))))
  }
  
  private def getBonus(altitude:Int):Double = {
    //http://wiki.teamliquid.net/starcraft/Terrain_Features#High_Ground
    val multiplier = 1.9
    return altitude match {
      case 0 => 1.0
      case 1 => 1.0 * multiplier
      case 2 => 1.0 * multiplier
      case 3 => 1.0 * multiplier * multiplier
      case 4 => 1.0 * multiplier * multiplier
      case _ => 1.0 * multiplier * multiplier * multiplier
    }
  }
}
