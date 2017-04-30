package Debugging.Visualizations.Views

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Mathematics.Pixels.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

object VisualizeHitPoints {
  
  def render() {
    With.units.all.foreach(renderHitPoints)
  }
  
  def renderHitPoints(unit:UnitInfo) {
    if (unit.invincible) return
    if ( ! unit.possiblyStillThere) return
    
    val width       = Math.min(48, Math.max(unit.unitClass.width / 2, 18))
    val height      = 4
    val denominator = unit.unitClass.maxTotalHealth + (if (unit.defensiveMatrixPoints > 0) width * 250 else 0)
    val widthHpMax  = width * unit.unitClass.maxHitPoints             / denominator
    val widthShMax  = width * unit.unitClass.maxShields               / denominator
    val widthDmMax  = if (unit.defensiveMatrixPoints > 0) width * 250 / denominator else 0
    val widthHpNow  = width * unit.hitPoints                          / denominator
    val widthShNow  = width * unit.shieldPoints                       / denominator
    val widthDmNow  = width * unit.defensiveMatrixPoints              / denominator
  
    val yStart      = unit.pixelCenter.y + unit.unitClass.height/2 - height
    val yEnd        = yStart + 3
    val xStart      = unit.pixelCenter.x - width/2
    val xStartHp    = xStart
    val xStartSh    = xStartHp + widthHpMax
    val xStartDm    = xStartSh + widthShMax
    
    val colorHp = Colors.BrightGreen
    val colorSh = Colors.BrightBlue
    val colorDm = Colors.BrightViolet
  
    DrawMap.box(new Pixel(xStart - 1, yStart - 1), new Pixel(xStart + width + 2, yEnd + 1), Color.Black, solid = true)
    DrawMap.box(new Pixel(xStartDm, yStart), new Pixel(xStartDm+widthDmMax, yEnd), colorDm, solid = false)
    DrawMap.box(new Pixel(xStartSh, yStart), new Pixel(xStartSh+widthShMax, yEnd), colorSh, solid = false)
    DrawMap.box(new Pixel(xStartHp, yStart), new Pixel(xStartHp+widthHpMax, yEnd), colorHp, solid = false)
    DrawMap.box(new Pixel(xStartDm, yStart), new Pixel(xStartDm+widthDmNow, yEnd), colorDm, solid = true)
    DrawMap.box(new Pixel(xStartSh, yStart), new Pixel(xStartSh+widthShNow, yEnd), colorSh, solid = true)
    DrawMap.box(new Pixel(xStartHp, yStart), new Pixel(xStartHp+widthHpNow, yEnd), colorHp, solid = true)
    
    if (unit.wounded) {
      DrawMap.box(new Pixel(xStart - 1, yStart - 1), new Pixel(xStart + width + 2, yEnd + 1), Colors.NeonRed, solid = false)
    }
  }
}
