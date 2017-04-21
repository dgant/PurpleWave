package Debugging.Visualizations.Views

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Mathematics.Pixels.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

object VisualizeHitPoints {
  
  def render() {
    With.units.all
      .foreach(renderHitPoints)
  }
  
  def renderHitPoints(unit:UnitInfo) {
    if (unit.invincible) return
    
    if (unit.unitClass.maxHitPoints > 0) {
      drawBox(unit, unit.hitPoints, unit.unitClass.maxHitPoints, 0, Colors.MediumGreen)
    }
  
    if (unit.unitClass.maxShields > 0) {
      drawBox(unit, unit.shieldPoints, unit.unitClass.maxShields, 4, Colors.MediumTeal)
    }
  }
  
  def drawBox(unit:UnitInfo, current:Int, max:Int, offsetY:Int, color:Color) {
    val width     = Math.min(48, Math.max(unit.unitClass.width, 24))
    val height    = 3
    val widthFull = width * current / max
    val x         = unit.pixelCenter.x - width/2
    val y         = unit.pixelCenter.y + unit.unitClass.height/2 - 8 + offsetY
    
    DrawMap.box(new Pixel(x, y), new Pixel(x+width,     y+height), color, solid = false)
    DrawMap.box(new Pixel(x, y), new Pixel(x+widthFull, y+height), color, solid = true)
    
  }
}
