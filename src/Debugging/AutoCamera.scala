package Debugging

import Lifecycle.With
import Mathematics.Pixels.{Pixel, Points}

class AutoCamera {
  
  var focus:Pixel = Points.middle
  
  def onFrame() {
    if ( ! With.configuration.camera) { return }
    
    if (With.battles.local.nonEmpty) {
      focus = With.battles.local.toVector.sortBy(_.focus.pixelDistanceFast(focus)).maxBy(b => b.enemy.strength * b.us.strength).us.vanguard
      setCameraSpeed(With.configuration.cameraDynamicSpeedSlowest)
    } else if (With.units.ours.nonEmpty) {
      focus = With.units.ours.minBy(_.pixelDistanceSquared(Points.middle)).pixelCenter
      setCameraSpeed(With.configuration.cameraDynamicSpeedFastest)
    }
    
    With.game.setScreenPosition(focus.subtract(320, 200).bwapi)
  }
  
  def setCameraSpeed(speed:Int) {
    if (With.configuration.cameraDynamicSpeed) {
      With.game.setLocalSpeed(speed)
    }
  }
}
