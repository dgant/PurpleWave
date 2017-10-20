package Debugging

import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints, Tile, TileRectangle}
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class Camera {
  
  private var tweenFrom   : Pixel     = SpecificPoints.middle
  private var focus       : Pixel     = With.self.startTile.pixelCenter
  private var focusUnit   : UnitInfo  = _
  
  private val refocusLimit      = 96
  private var focusFrame        = -240
  private val tweenFrames       = 24
  private var impatienceFrames  = 24 * 10
  
  
  var interestByBattle  : Map[Battle,   Double] = Map.empty
  var interestByUnit    : Map[UnitInfo, Double] = Map.empty
  var visibleArea       : TileRectangle         = TileRectangle(Tile(0, 0), Tile(0, 0))
  var visibleUnits      : Set[UnitInfo]         = Set.empty
  
  private def totalInterest(unit: UnitInfo): Double = {
    val interestBattle  = if (unit.battle.isDefined) interestByBattle.getOrElse(unit.battle.get, 0.0) else 0.0
    val interestUnit    = interestByUnit(unit)
    interestBattle * interestUnit * unit.subjectiveValue
  }
  
  def onFrame() {
    if ( ! With.configuration.camera) { return }
    val interestGain  = 1.0 / impatienceFrames
    val interestDecay = 2 * interestGain
  
    interestByBattle  = With.battles.local.map(b => (b, 0.0 + b.enemy.units.map(_.subjectiveValue).sum * b.us.units.map(_.subjectiveValue).sum)).toMap
    interestByUnit = With.units.ours
      .map(unit => {
        val interestOld = interestByUnit.getOrElse(unit, 1.0)
        val interestNew = Math.min(1.0, interestOld + interestGain - (if (visibleUnits.contains(unit)) interestDecay else 0.0))
        (unit, interestNew)
      }).toMap
    
    val eligibleUnits       = With.units.ours.filterNot(unit => unit.is(Protoss.Interceptor) || unit.is(Protoss.Scarab) || unit.is(Terran.SpiderMine))
    val unitInterests       = With.units.ours.map(unit => (unit, totalInterest(unit)))
    val mostInterestingUnit = ByOption.maxBy(unitInterests)(_._2)
    
    if (mostInterestingUnit.isDefined) {
      focusOn(mostInterestingUnit.get._1)
    }
  
    if (focusUnit != null && focusUnit.battle.isDefined) {
      setCameraSpeed(With.configuration.cameraDynamicSpeedSlowest)
    }
    else {
      setCameraSpeed(With.configuration.cameraDynamicSpeedFastest)
    }
    
    tween()
  
    visibleArea   = TileRectangle(With.viewport.start.tileIncluding, With.viewport.end.tileIncluding)
    visibleUnits  = With.units.inRectangle(visibleArea).toSet
  }
  
  def focusOn(unit: UnitInfo) {
    if (With.framesSince(focusFrame) < refocusLimit) return
    focusUnit = unit
    focusFrame = With.frame
    tweenFrom = focus
  }
  
  def setCameraSpeed(speed: Int) {
    if (With.configuration.cameraDynamicSpeed) {
      With.game.setLocalSpeed(speed)
    }
  }
  
  def tween() {
    if (focusUnit != null && focusUnit.alive) {
      val start = focusUnit.pixelCenter
      focus = if (focusUnit.battle.exists(_.enemy.units.nonEmpty)) start.project(focusUnit.battle.get.focus, 120) else start
      val tweenFraction = Math.max(0.0, Math.min(1.0, (With.framesSince(focusFrame) + 1).toDouble/tweenFrames))
      val tweenPoint    = tweenFrom.project(focus, tweenFrom.pixelDistanceSlow(focus) * tweenFraction)
      With.viewport.centerOn(tweenPoint)
    }
  }
}
