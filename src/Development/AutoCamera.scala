package Development

import Geometry.Positions
import Startup.With
import bwapi.Position
import Utilities.Enrichment.EnrichPosition._

class AutoCamera {
  
  var focus:Position = Positions.middle
  
  def onFrame() {
    if ( ! With.configuration.enableCamera) { return }
    if (With.simulator.battles.nonEmpty) {
      With.simulator.battles.maxBy(b => b.enemyScore * b.ourScore)
    } else {
      focus = With.units.ours.minBy(_.distanceSquared(Positions.middle)).position
    }
    
    With.game.setScreenPosition(focus.subtract(320, 240))
  }
}
