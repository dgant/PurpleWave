package Debugging

import Lifecycle.With
import Mathematics.Pixels.{Pixel, Points}

class AutoCamera {
  
  var focus:Pixel = Points.middle
  
  private val refocusLimit = 48
  private var lastJumpFrame = -240
  private val tween
  
  def onFrame() {
    
    if ( ! With.configuration.camera) { return }
  
    var newFocus = focus
  
    if (With.battles.local.nonEmpty) {
      newFocus = With.battles.local.toVector.sortBy(_.focus.pixelDistanceFast(focus)).maxBy(b => b.enemy.strength * b.us.strength).us.vanguard
      setCameraSpeed(With.configuration.cameraDynamicSpeedSlowest)
    } else if (With.units.ours.nonEmpty) {
      newFocus = With.units.ours.toList
        .sortBy(_.pixelDistanceSquared(With.intelligence.mostBaselikeEnemyPixel.pixelCenter))
        .sortBy( ! _.canAttackThisSecond)
        .sortBy( ! _.canMoveThisFrame)
        .head
        .project(refocusLimit)
      setCameraSpeed(With.configuration.cameraDynamicSpeedFastest)
    }
  
    if (focus.pixelDistanceFast(newFocus) < 96.0 || With.frame - lastJumpFrame > refocusLimit ) {
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
