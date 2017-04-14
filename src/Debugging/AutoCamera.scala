package Debugging

import Lifecycle.With
import Mathematics.Pixels.{Pixel, Points}

class AutoCamera {
  
  var focus:Pixel = Points.middle
  
  private var lastJumpFrame = -240
  
  def onFrame() {
    
    if ( ! With.configuration.camera) { return }
  
    var newFocus = focus
  
    if (With.battles.local.nonEmpty) {
      newFocus = With.battles.local.toVector.sortBy(_.focus.pixelDistanceFast(focus)).maxBy(b => b.enemy.strength * b.us.strength).us.vanguard
      setCameraSpeed(With.configuration.cameraDynamicSpeedSlowest)
    } else if (With.units.ours.nonEmpty) {
      newFocus = With.units.ours.toList
        .sortBy(_.canMove)
        .sortBy(_.pixelDistanceSquared(With.intelligence.mostBaselikeEnemyPixel.pixelCenter))
        .head
        .pixelCenter
      setCameraSpeed(With.configuration.cameraDynamicSpeedFastest)
    }
  
    if (focus.pixelDistanceFast(newFocus) < 96.0 || With.frame - lastJumpFrame > 48 ) {
      focus = newFocus
      lastJumpFrame = With.frame
    }
    
    With.game.setScreenPosition(focus.subtract(320, 200).bwapi)
  }
  
  def setCameraSpeed(speed:Int) {
    if (With.configuration.cameraDynamicSpeed) {
      With.game.setLocalSpeed(speed)
    }
  }
}
