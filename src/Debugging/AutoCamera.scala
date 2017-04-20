package Debugging

import Lifecycle.With
import Mathematics.Pixels.{Pixel, Points}

class AutoCamera {
  
  var origin  : Pixel = Points.middle
  var focus   : Pixel = Points.middle
  
  private val refocusLimit = 48
  private var focusFrame = -240
  private val tweenFrames = 12
  
  def onFrame() {
    
    if ( ! With.configuration.camera) { return }
  
    var newFocus = focus
  
    if (With.battles.local.nonEmpty) {
      
      moveFocus(With.battles.local
        .sortBy(_.focus.pixelDistanceFast(focus))
        .maxBy(b => b.enemy.strength * b.us.strength).us.vanguard)
      
      setCameraSpeed(With.configuration.cameraDynamicSpeedSlowest)
    } else if (With.units.ours.nonEmpty) {
      
      moveFocus(With.units.ours.toList
        .sortBy(_.pixelDistanceSquared(With.intelligence.mostBaselikeEnemyPixel.pixelCenter))
        .sortBy( ! _.canAttackThisSecond)
        .sortBy( ! _.canMoveThisFrame)
        .head
        .project(refocusLimit))
      
      setCameraSpeed(With.configuration.cameraDynamicSpeedFastest)
    }
    
    tween()
  }
  
  def moveFocus(to:Pixel) {
    focusFrame = With.frame
    origin = With.viewport.center
    focus = to
  }
  
  def setCameraSpeed(speed:Int) {
    if (With.configuration.cameraDynamicSpeed) {
      With.game.setLocalSpeed(speed)
    }
  }
  
  def tween() {
    val tweenFraction = Math.sqrt(Math.max(0.0, Math.min(1.0, (With.frame - focusFrame).toDouble/tweenFrames)))
    val tweenPoint    = origin.project(focus, origin.pixelDistanceSlow(focus) * tweenFraction)
    With.viewport.centerOn(tweenPoint)
  }
}
