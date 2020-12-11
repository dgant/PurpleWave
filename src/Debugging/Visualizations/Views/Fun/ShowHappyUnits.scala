package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

object ShowHappyUnits extends View {
  
  override def renderMap() {
    
    With.units.all
      .filter(unit =>
        unit.alive &&
        unit.likelyStillThere &&
        (unit.complete || unit.unitClass.isBuilding))
      .toVector
      .sortBy(_.y)
      .sortBy(_.flying)
      .foreach(renderUnit)
    
    With.game.drawTextScreen(408, 4, "Candy: ")
    With.game.drawTextScreen(481, 4, "Soda: ")
    With.game.drawTextScreen(541, 4, "Friends: ")
  }
  
  def renderUnit(unit:UnitInfo) {
    
    val color = unit.teamColor
    val isActiveBuilding = unit.friendly.nonEmpty && unit.friendly.get.trainingQueue.nonEmpty
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
    val farLeft           = left    - unit.unitClass.width / 8
    val farRight          = right   + unit.unitClass.width / 8
    val leftPerspective   = left    //+ unit.unitClass.width / 8
    val rightPerspective  = right   //- unit.unitClass.width / 8
    val waist             = top + unit.unitClass.height / 4
    val apex              = top - unit.unitClass.height / 4
  
    drawSolidTriangle(
      Pixel(farLeft, bottom),
      Pixel(farRight, bottom),
      Pixel(middle, apex),
      color)
    drawSolidTriangle(
      Pixel(farLeft, bottom),
      Pixel(leftPerspective, waist),
      Pixel(middle, apex),
      color)
    drawSolidTriangle(
      Pixel(farRight, bottom),
      Pixel(rightPerspective, waist),
      Pixel(middle, apex),
      color)
    DrawMap.label(
      unit.unitClass.toString
        .replaceAll("Vespene", "Soda")
        .replaceAll("Mineral", "Candy")
        .replaceAll("Assimilator", "Soda Assimilator")
        .replaceAll("Extractor", "Soda Extractor")
        .replaceAll("Refinery", "Soda Refinery")
        .replaceAll("Critter Rhynadon", "Snuggles the Rhino")
        .replaceAll("Critter Ursadon", "Cupcake Cuddlemonster")
        .replaceAll("Critter Kakaru", "Flying Dumpling"),
      Pixel(middle, bottom + 9))

    val eyeLeft         = middle  + unit.unitClass.width  / 5
    val eyeRight        = middle  - unit.unitClass.width  / 5
    val eyeLevel        = top     + unit.unitClass.height / 2
    val mouthLeft       = left    + unit.unitClass.width  / 3
    val mouthRight      = right   - unit.unitClass.width  / 3
    val mouthAngleLeft  = left    + unit.unitClass.width  * 5 / 12
    val mouthAngleRight = right   - unit.unitClass.width  * 5 / 12
    var mouthTop        = top     + unit.unitClass.height * 6 / 10
    var mouthBottom     = top     + unit.unitClass.height * 7 / 10
    
    if (unit.unitClass.rawCanAttack && ! unit.unitClass.isWorker) {
      val foreheadLow   = eyeLevel - unit.unitClass.height / 8
      val foreheadHigh  = eyeLevel - unit.unitClass.height / 4
      DrawMap.line(
        Pixel(mouthLeft, foreheadHigh),
        Pixel(middle,    foreheadLow),
        color)
      DrawMap.line(
        Pixel(mouthRight,  foreheadHigh),
        Pixel(middle,      foreheadLow),
        color)
    }
    
    if (unit.complete) {
      if (unit.totalHealth < unit.unitClass.maxTotalHealth / 2) {
        val swap = mouthBottom
        mouthBottom = mouthTop
        mouthTop = swap
      }
      else if (unit.totalHealth < unit.unitClass.maxTotalHealth * 3 / 4) {
        val mouthMiddle = (mouthBottom + mouthTop)/2
        mouthTop = mouthMiddle
        mouthBottom = mouthMiddle
      }
    }
    
    if (unit.complete) {
      DrawMap.box(Pixel(eyeLeft-1,  eyeLevel-1), Pixel(eyeLeft+1,  eyeLevel+1), color, solid = true)
      DrawMap.box(Pixel(eyeRight-1, eyeLevel-1), Pixel(eyeRight+1, eyeLevel+1), color, solid = true)
    }
    else {
      DrawMap.line(Pixel(eyeLeft-1,  eyeLevel+1),  Pixel(eyeLeft+1,  eyeLevel+1), color)
      DrawMap.line(Pixel(eyeLeft-1,  eyeLevel+1),  Pixel(eyeLeft-3,  eyeLevel-1), color)
      DrawMap.line(Pixel(eyeLeft+1,  eyeLevel+1),  Pixel(eyeLeft+3,  eyeLevel-1), color)
      DrawMap.line(Pixel(eyeRight-1, eyeLevel+1),  Pixel(eyeRight+1, eyeLevel+1), color)
      DrawMap.line(Pixel(eyeRight-1, eyeLevel+1),  Pixel(eyeRight-3, eyeLevel-1), color)
      DrawMap.line(Pixel(eyeRight+1, eyeLevel+1),  Pixel(eyeRight+3, eyeLevel-1), color)
      
      DrawMap.text(Pixel(right, top), "Z")
      (1 to (1 + animationFrame/16 % 4)).foreach(i => DrawMap.text(Pixel(right + i * 6, top - i * 2), "z"))
    }
    
    DrawMap.line(
      Pixel(mouthLeft, mouthTop),
      Pixel(mouthAngleLeft, mouthBottom),
      color)
    DrawMap.line(
      Pixel(mouthAngleLeft, mouthBottom),
      Pixel(mouthAngleRight, mouthBottom),
      color)
    DrawMap.line(
      Pixel(mouthRight, mouthTop),
      Pixel(mouthAngleRight, mouthBottom),
      color)
    
    if (unit.carryingMinerals) {
      DrawMap.circle(unit.pixelCenter.add(-10, 0), 4, Colors.NeonTeal, true)
    }
    if (unit.carryingGas) {
      DrawMap.circle(unit.pixelCenter.add(-10, 0), 4, Colors.NeonGreen, true)
    }
  }
  
  def drawSolidTriangle(p1: Pixel, p2: Pixel, p3: Pixel, foreground: Color, background: Color = Color.Black) {
    DrawMap.triangle(p1, p2, p3, background, true)
    DrawMap.triangle(p1, p2, p3, foreground, false)
  }
}

