package Debugging.Visualization

import ProxyBwapi.UnitInfo.UnitInfo
import Startup.With
import bwapi.{Color, Position}
import Utilities.TypeEnrichment.EnrichPosition._

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
    With.game.drawBoxScreen   (5,      5,      5 + 20, 5 + 20,        DrawMap.playerColor(With.self),       true)
    With.game.drawBoxScreen   (5,      5 + 25, 5 + 20, 5 + 20 + 25,   DrawMap.playerColor(With.game.enemy), true)
    With.game.drawTextScreen  (5 + 25, 5,                             With.game.self.getName + " vs. ")
    With.game.drawTextScreen  (5 + 25, 5 + 25,                        With.game.enemy.getName)
    With.game.setTextSize(bwapi.Text.Size.Enum.Default)
    With.game.drawTextScreen  (5,      5 + 50,                   "PurpleWave is displaying this game in Arcade Happy Vision!")
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
    With.game.drawTextScreen(410, 4, "Candy: ")
    With.game.drawTextScreen(482, 4, "Soda: ")
    With.game.drawTextScreen(540, 4, "Friends: ")
  }
  
  def renderUnit(unit:UnitInfo) {
    
    val color = DrawMap.playerColor(unit.player)
    val isActiveBuilding = unit.friendly.nonEmpty && unit.friendly.get.trainingQueue.size > 0
    val animationFrame = unit.left + unit.top + With.frame
    
    val verticalBonus =
      if (unit.flying) 24
      else if (isActiveBuilding) (Math.sin(animationFrame/8) * 6).toInt
      else 0
    
    val horizontalBonus = 0
  
    val left              = horizontalBonus + unit.left
    val right             = horizontalBonus + unit.right
    val top               = verticalBonus + unit.top
    val bottom            = verticalBonus + unit.bottom
    val middle            = (left + right)/2
    val leftPerspective   = left   + unit.unitClass.width / 8
    val rightPerspective  = right  - unit.unitClass.width / 8
    val waist             = top + unit.unitClass.height / 4
    val apex              = top - unit.unitClass.height / 4
    
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
      new Position(unit.left, bottom),
      new Position(right, bottom),
      color)
    DrawMap.label(
      unit.unitClass.toString
        .replaceAll("Vespene", "Soda")
        .replaceAll("Mineral", "Candy")
        .replaceAll("Assimilator", "Soda Assimilator")
        .replaceAll("Extractor", "Soda Extractor")
        .replaceAll("Refinery", "Soda Refinery"),
      new Position(middle, bottom + 9))

    val eyeLeft         = middle  + unit.unitClass.width  / 5
    val eyeRight        = middle  - unit.unitClass.width  / 5
    val eyeLevel        = top     + unit.unitClass.height / 2
    val mouthLeft       = left    + unit.unitClass.width  / 3
    val mouthRight      = right   - unit.unitClass.width  / 3
    val mouthAngleLeft  = left    + unit.unitClass.width  * 5 / 12
    val mouthAngleRight = right   - unit.unitClass.width  * 5 / 12
    var mouthTop        = top     + unit.unitClass.height * 6 / 10
    var mouthBottom     = top     + unit.unitClass.height * 7 / 10
    
    if (unit.unitClass.canAttack && ! unit.unitClass.isWorker) {
      val foreheadLow   = eyeLevel - unit.unitClass.height / 8
      val foreheadHigh  = eyeLevel - unit.unitClass.height / 4
      DrawMap.line(
        new Position(mouthLeft, foreheadHigh),
        new Position(middle,    foreheadLow),
        color)
      DrawMap.line(
        new Position(mouthRight,  foreheadHigh),
        new Position(middle,      foreheadLow),
        color)
    }
    
    if (unit.complete) {
      if (unit.totalHealth < unit.unitClass.maxTotalHealth * 3 / 4) {
        val swap = mouthBottom
        mouthBottom = mouthTop
        mouthTop = swap
      }
      else if (unit.totalHealth < unit.unitClass.maxTotalHealth / 2) {
        val mouthMiddle = (mouthBottom + mouthTop)/2
        mouthTop = mouthMiddle
        mouthBottom = mouthMiddle
      }
    }
    
    if (unit.complete) {
      DrawMap.box(new Position(eyeLeft-1,  eyeLevel-1), new Position(eyeLeft+1,  eyeLevel+1), color, true)
      DrawMap.box(new Position(eyeRight-1, eyeLevel-1), new Position(eyeRight+1, eyeLevel+1), color, true)
    }
    else {
      DrawMap.line(new Position(eyeLeft-1,  eyeLevel+1),  new Position(eyeLeft+1,  eyeLevel+1), color)
      DrawMap.line(new Position(eyeLeft-1,  eyeLevel+1),  new Position(eyeLeft-3,  eyeLevel-1), color)
      DrawMap.line(new Position(eyeLeft+1,  eyeLevel+1),  new Position(eyeLeft+3,  eyeLevel-1), color)
      DrawMap.line(new Position(eyeRight-1, eyeLevel+1),  new Position(eyeRight+1, eyeLevel+1), color)
      DrawMap.line(new Position(eyeRight-1, eyeLevel+1),  new Position(eyeRight-3, eyeLevel-1), color)
      DrawMap.line(new Position(eyeRight+1, eyeLevel+1),  new Position(eyeRight+3, eyeLevel-1), color)
      
      DrawMap.text(new Position(right, top), "Z")
      (1 to (1 + animationFrame/16 % 4)).foreach(i => DrawMap.text(new Position(right + i * 6, top - i * 2), "z"))
    }
    
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
    
    if (unit.carryingMinerals) {
      DrawMap.circle(unit.pixelCenter.add(-10, 0), 4, Color.Cyan, true)
    }
    if (unit.carryingGas) {
      DrawMap.circle(unit.pixelCenter.add(-10, 0), 4, Color.Green, true)
    }
  }
}

