package Debugging.Visualizations.Views

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import ProxyBwapi.UnitInfo.UnitInfo
import Lifecycle.With
import Mathematics.Pixels.Pixel
import bwapi.Color

object VisualizeVectorUnits {
  
  def render() {
    
    With.units.all
      .filter(unit =>
        unit.alive &&
        unit.possiblyStillThere &&
        (unit.complete || unit.unitClass.isBuilding))
      .toVector
      .sortBy(_.flying)
      .sortBy(_.y)
      .foreach(renderUnit)
    
    With.game.drawTextScreen(408, 4, "Candy: ")
    With.game.drawTextScreen(481, 4, "Soda: ")
    With.game.drawTextScreen(541, 4, "Friends: ")
  }
  
  def renderUnit(unit:UnitInfo) {
    
    val color = unit.player.colorNeon
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
    val farLeft           = left    - unit.unitClass.width / 8
    val farRight          = right   + unit.unitClass.width / 8
    val leftPerspective   = left    //+ unit.unitClass.width / 8
    val rightPerspective  = right   //- unit.unitClass.width / 8
    val waist             = top + unit.unitClass.height / 4
    val apex              = top - unit.unitClass.height / 4
  
    drawSolidTriangle(
      new Pixel(farLeft, bottom),
      new Pixel(farRight, bottom),
      new Pixel(middle, apex),
      color)
    drawSolidTriangle(
      new Pixel(farLeft, bottom),
      new Pixel(leftPerspective, waist),
      new Pixel(middle, apex),
      color)
    drawSolidTriangle(
      new Pixel(farRight, bottom),
      new Pixel(rightPerspective, waist),
      new Pixel(middle, apex),
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
      new Pixel(middle, bottom + 9))

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
        new Pixel(mouthLeft, foreheadHigh),
        new Pixel(middle,    foreheadLow),
        color)
      DrawMap.line(
        new Pixel(mouthRight,  foreheadHigh),
        new Pixel(middle,      foreheadLow),
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
      DrawMap.box(new Pixel(eyeLeft-1,  eyeLevel-1), new Pixel(eyeLeft+1,  eyeLevel+1), color, true)
      DrawMap.box(new Pixel(eyeRight-1, eyeLevel-1), new Pixel(eyeRight+1, eyeLevel+1), color, true)
    }
    else {
      DrawMap.line(new Pixel(eyeLeft-1,  eyeLevel+1),  new Pixel(eyeLeft+1,  eyeLevel+1), color)
      DrawMap.line(new Pixel(eyeLeft-1,  eyeLevel+1),  new Pixel(eyeLeft-3,  eyeLevel-1), color)
      DrawMap.line(new Pixel(eyeLeft+1,  eyeLevel+1),  new Pixel(eyeLeft+3,  eyeLevel-1), color)
      DrawMap.line(new Pixel(eyeRight-1, eyeLevel+1),  new Pixel(eyeRight+1, eyeLevel+1), color)
      DrawMap.line(new Pixel(eyeRight-1, eyeLevel+1),  new Pixel(eyeRight-3, eyeLevel-1), color)
      DrawMap.line(new Pixel(eyeRight+1, eyeLevel+1),  new Pixel(eyeRight+3, eyeLevel-1), color)
      
      DrawMap.text(new Pixel(right, top), "Z")
      (1 to (1 + animationFrame/16 % 4)).foreach(i => DrawMap.text(new Pixel(right + i * 6, top - i * 2), "z"))
    }
    
    DrawMap.line(
      new Pixel(mouthLeft, mouthTop),
      new Pixel(mouthAngleLeft, mouthBottom),
      color)
    DrawMap.line(
      new Pixel(mouthAngleLeft, mouthBottom),
      new Pixel(mouthAngleRight, mouthBottom),
      color)
    DrawMap.line(
      new Pixel(mouthRight, mouthTop),
      new Pixel(mouthAngleRight, mouthBottom),
      color)
    
    if (unit.carryingMinerals) {
      DrawMap.circle(unit.pixelCenter.add(-10, 0), 4, Colors.NeonTeal, true)
    }
    if (unit.carryingGas) {
      DrawMap.circle(unit.pixelCenter.add(-10, 0), 4, Colors.NeonGreen, true)
    }
  }
  
  def drawSolidTriangle(p1:Pixel, p2:Pixel, p3:Pixel, foreground:Color, background:Color = Color.Black) {
    DrawMap.triangle(p1, p2, p3, background, true)
    DrawMap.triangle(p1, p2, p3, foreground, false)
  }
}

