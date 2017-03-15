package Information.Grids.Concrete

import Information.Grids.Abstract.GridDouble
import Startup.With

class GridAltitudeBonus extends GridDouble {
  
  override def onInitialization() {
    tiles.foreach(position => set(position, getBonus(With.game.getGroundHeight(position))))
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
