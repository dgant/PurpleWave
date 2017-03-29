package Debugging

import Geometry.Positions
import Startup.With
import bwapi.Position
import Utilities.EnrichPosition._

class AutoCamera {
  
  var focus:Position = Positions.middle
  
  def onFrame() {
    if ( ! With.configuration.enableCamera) { return }
    
    if (With.battles.local.nonEmpty) {
      focus = With.battles.local.toList.sortBy(_.focus.getDistance(focus)).maxBy(b => b.enemy.strength * b.us.strength).us.vanguard
      setCameraSpeed(With.configuration.cameraDynamicSpeedSlowest)
    } else if (With.units.ours.nonEmpty) {
      focus = With.units.ours.minBy(_.pixelDistanceSquared(Positions.middle)).pixelCenter
      setCameraSpeed(With.configuration.cameraDynamicSpeedFastest)
    }
    
    With.game.setScreenPosition(focus.subtract(320, 200))
  }
  
  def setCameraSpeed(speed:Int) {
    if (With.configuration.cameraDynamicSpeed) {
      With.game.setLocalSpeed(speed)
    }
  }
}
