package Geometry.Grids.Real

import Geometry.Grids.Abstract.GridDouble
import Startup.With

class GridAltitudeBonus extends GridDouble {
  
  override def onInitialization() {
    positions.foreach(position => set(position, _getBonus(With.game.getGroundHeight(position))))
  }
  
  def _getBonus(altitude:Int):Double = {
    val multiplier = 1.3
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
