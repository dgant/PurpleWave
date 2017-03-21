package Debugging.Visualization

import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With
import bwapi.{Color, Position}

object VisualizeVectorUnits {
  
  def render() {
    With.game.drawBoxScreen(0, 0, 1500, 1200, Color.Black, true)
    
    With.units.all
      .filter(unit =>
        unit.alive &&
        unit.possiblyStillThere &&
        (unit.complete || unit.unitClass.isBuilding))
      .foreach(renderUnit)
    
    With.game.setTextSize(bwapi.Text.Size.Enum.Large)
    With.game.drawTextScreen(10, 5, With.game.self.getName + " vs. " + With.game.enemy.getName)
    With.game.setTextSize(bwapi.Text.Size.Enum.Default)
    With.game.drawTextScreen(10, 30, "PurpleWave is displaying this game in Arcade Happy Vision")
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  }
  
  def renderUnit(unit:UnitInfo) {
    
    val color = DrawMap.playerColor(unit.player)
    
    val flyingBonus = if (unit.flying) 24 else 0
    val leftPerspective   = unit.left   + unit.unitClass.width / 8
    val rightPerspective  = unit.right  - unit.unitClass.width / 8
    val left = unit.left
    val right = unit.right
    val middle = (unit.left + unit.right)/2
    val top    = flyingBonus + unit.top
    val bottom = flyingBonus + unit.bottom
    val waist  = flyingBonus + unit.top + unit.unitClass.height / 4
    val apex   = flyingBonus + unit.top - unit.unitClass.height / 4
    
    DrawMap.line(
      new Position(left, bottom),
      new Position(middle, apex),
      color)
    DrawMap.line(
      new Position(right, bottom),
      new Position(middle, apex),
      color)
    DrawMap.line(
      new Position(leftPerspective, waist),
      new Position(middle, apex),
      color)
    DrawMap.line(
      new Position(rightPerspective, waist),
      new Position(middle, apex),
      color)
    DrawMap.line(
      new Position(leftPerspective, waist),
      new Position(left, bottom),
      color)
    DrawMap.line(
      new Position(rightPerspective, waist),
      new Position(right, bottom),
      color)
    DrawMap.line(
      new Position(unit.left, unit.bottom),
      new Position(right, bottom),
      color)
    DrawMap.label(unit.unitClass.toString, new Position(middle, unit.bottom + 9))

    val eyeLeft         = left  + unit.unitClass.width * 5 / 12
    val eyeRight        = right - unit.unitClass.width * 5 / 12
    val eyeLevel        = flyingBonus + top + unit.unitClass.height / 2
    val mouthLeft       = left  + unit.unitClass.width / 3
    val mouthRight      = right - unit.unitClass.width / 3
    val mouthAngleLeft  = left  + unit.unitClass.width * 5 / 12
    val mouthAngleRight = right - unit.unitClass.width * 5 / 12
    var mouthTop        = top + unit.unitClass.height * 6 / 10
    var mouthBottom     = top + unit.unitClass.height * 7 / 10
    
    if (unit.unitClass.canAttack && ! unit.unitClass.isWorker) {
      var swap = mouthBottom
      mouthBottom = mouthTop
      mouthTop = swap
    }
    
    DrawMap.box(new Position(eyeLeft-1, eyeLevel-1), new Position(eyeLeft,    eyeLevel), color)
    DrawMap.box(new Position(eyeRight,  eyeLevel-1), new Position(eyeRight+1, eyeLevel), color)
    DrawMap.line(
      new Position(mouthLeft, mouthTop),
      new Position(mouthAngleLeft, mouthBottom),
      color)
    DrawMap.line(
      new Position(mouthAngleLeft, mouthBottom),
      new Position(mouthAngleRight, mouthBottom),
      color)
    DrawMap.line(
      new Position(mouthRight, mouthTop),
      new Position(mouthAngleRight, mouthBottom),
      color)
  }
}

