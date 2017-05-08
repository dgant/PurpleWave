package Debugging

import Lifecycle.With
import Mathematics.Pixels.{Pixel, Points}
import ProxyBwapi.UnitInfo.UnitInfo

class AutoCamera {
  
  private var tweenFrom: Pixel = Points.middle
  private var focus = With.self.startTile.pixelCenter
  
  private val refocusLimit = 96
  private var focusFrame = -240
  private val tweenFrames = 24
  private var focusUnit:UnitInfo = null
  
  def onFrame() {
    
    if ( ! With.configuration.camera) { return }
    
    val battles = With.battles.local
  
    if (battles.nonEmpty) {
      
      val battle = battles.maxBy(b => b.enemy.units.size * b.us.units.size)
  
      setCameraSpeed(With.configuration.cameraDynamicSpeedSlowest)
      focusOn(battle.us.units.minBy(_.pixelDistanceSquared(battle.us.vanguard)))
        
    } else if (With.units.ours.nonEmpty) {
  
      setCameraSpeed(With.configuration.cameraDynamicSpeedFastest)
      focusOn(With.units.ours.toList
        .sortBy(_.pixelDistanceSquared(With.intelligence.mostBaselikeEnemyTile.pixelCenter))
        .sortBy( ! _.canAttackThisSecond)
        .sortBy( ! _.canMoveThisFrame)
        .head)
    }
    
    tween()
  }
  
  def focusOn(unit:UnitInfo) {
    if (With.frame - focusFrame < refocusLimit) return
    focusUnit = unit
    focusFrame = With.frame
    tweenFrom = focus
  }
  
  def setCameraSpeed(speed:Int) {
    if (With.configuration.cameraDynamicSpeed) {
      With.game.setLocalSpeed(speed)
    }
  }
  
  def tween() {
    if (focusUnit != null && focusUnit.alive) {
      focus = focusUnit.pixelCenter
      val tweenFraction = Math.max(0.0, Math.min(1.0, (With.frame - focusFrame + 1).toDouble/tweenFrames))
      val tweenPoint    = tweenFrom.project(focus, tweenFrom.pixelDistanceSlow(focus) * tweenFraction)
      With.viewport.centerOn(tweenPoint)
    }
  }
}
