package Development

import Geometry.Positions
import Startup.With
import bwapi.Position
import Utilities.Enrichment.EnrichPosition._

class AutoCamera {
  
  var focus:Position = Positions.middle
  
  def onFrame() {
    if ( ! With.configuration.enableCamera) { return }
    if (With.battles.battles.nonEmpty) {
      focus = With.battles.battles.toList.sortBy(_.focus.getDistance(focus)).maxBy(b => b.enemy.strength * b.us.strength).us.vanguard
      With.game.setLocalSpeed(30)
    } else {
      focus = With.units.ours.minBy(_.distanceSquared(Positions.middle)).position
      With.game.setLocalSpeed(0)
    }
    
    With.game.setScreenPosition(focus.subtract(320, 200))
  }
}
