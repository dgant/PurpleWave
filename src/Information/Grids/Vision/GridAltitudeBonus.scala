package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridDouble
import Lifecycle.With

class GridAltitudeBonus extends AbstractGridDouble {
  
  override def onInitialization() {
    tiles.foreach(tile => set(tile, getBonus(With.game.getGroundHeight(tile.bwapi))))
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
