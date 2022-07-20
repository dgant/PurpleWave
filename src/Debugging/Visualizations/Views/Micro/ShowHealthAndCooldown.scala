package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

object ShowHealthAndCooldown extends DebugView {
  
  override def renderMap(): Unit = { With.units.all.foreach(renderUnit) }
  
  def renderUnit(unit: UnitInfo): Unit = {
    if ( ! With.viewport.contains(unit.pixel)) return
    if ( ! unit.visible) return
    if (unit.invincible) return
    if (unit.unitClass.maxTotalHealth == 0) return
    
    val width           = Maff.clamp(unit.unitClass.width, 18, 48)
    val marginTopHp     = 3
    val denominator     = unit.unitClass.maxTotalHealth + width * 250 * Maff.fromBoolean(unit.matrixPoints > 0)
    val widthHpMax      = width * unit.unitClass.maxHitPoints / denominator
    val widthShMax      = width * unit.unitClass.maxShields / denominator
    val widthDmMax      = width * 250 / denominator * Maff.fromBoolean(unit.matrixPoints > 0)
    val widthHpNow      = width * unit.hitPoints / denominator
    val widthShNow      = width * unit.shieldPoints / denominator
    val widthDmNow      = width * unit.matrixPoints / denominator
    val widthEnergyNow  = if (unit.energyMax == 0) 0 else Math.min(width, width * unit.energy / unit.energyMax) // Min, because I haven't yet accounted for energy max upgrades

    val widthCooldownButton   = 3
    val widthCooldown         = width - 2 * widthCooldownButton - 2
    val widthCooldownNow      = widthCooldown * Math.max(unit.cooldownLeft, unit.cooldownSpell) / Math.max(1, unit.cooldownMaxAirGround) //TODO: Max spell cooldown?
    
    val yStartHp              = unit.pixel.y + unit.unitClass.height / 2 - marginTopHp
    val yEndHp                = yStartHp + 4
    val yStartEnergy          = yEndHp + 2
    val yEndEnergy            = yStartEnergy + 4
    val yStartCooldown        = if (unit.energyMax > 0) yEndEnergy + 3 else yEndHp + 3
    val yEndCooldown          = yStartCooldown + 3
    val xStart                = unit.pixel.x - width / 2
    val xStartSh              = xStart + widthHpMax
    val xStartDm              = xStartSh + widthShMax
    val xStartCooldown        = xStart
    val xStartCooldownButton0 = xStart + widthCooldown + 1
    val xStartCooldownButton1 = xStart + widthCooldown + 2 + widthCooldownButton
    
    val colorHp = Colors.NeonGreen
    val colorSh = Colors.NeonBlue
    val colorDm = Colors.NeonViolet
    
    if (unit.totalHealth != unit.unitClass.maxTotalHealth) {
      DrawMap.box(Pixel(xStart - 1, yStartHp - 1), Pixel(xStart + width + 2, yEndHp + 1), Color.Black, solid = true)
      /*
      if (unit.matrixPoints > 0) {
        DrawMap.box(Pixel(xStartDm, yStartHp), Pixel(xStartDm + widthDmMax, yEndHp), colorDm, solid = false)
      }
      if (unit.unitClass.maxShields > 0) {
        DrawMap.box(Pixel(xStartSh, yStartHp), Pixel(xStartSh + widthShMax, yEndHp), colorSh, solid = false)
      }
      DrawMap.box(Pixel(xStart, yStartHp), Pixel(xStart + widthHpMax, yEndHp), colorHp, solid = false)
      */
      if (unit.matrixPoints > 0) {
        DrawMap.box(Pixel(xStartDm, yStartHp), Pixel(xStartDm + widthDmNow, yEndHp), colorDm, solid = true)
      }
      if (unit.unitClass.maxShields > 0) {
        DrawMap.box(Pixel(xStartSh, yStartHp), Pixel(xStartSh + widthShNow, yEndHp), colorSh, solid = true)
      }
      DrawMap.box(Pixel(xStart, yStartHp), Pixel(xStart + widthHpNow, yEndHp), colorHp, solid = true)
  
      val healthBarEvery = Math.max(3, width * 25 / (unit.unitClass.maxTotalHealth + unit.matrixPoints))
      (0 until width by healthBarEvery).foreach(healthX => {
        val x = xStart + healthX - 1
        DrawMap.line(Pixel(x, yStartHp), Pixel(x, yEndHp), Color.Black)
      })
    }
    
    if (unit.energyMax > 0) {
      DrawMap.box(Pixel(xStart - 1, yStartEnergy - 1), Pixel(xStart + width + 2, yEndEnergy + 1), Color.Black, solid = true)
      (25 until unit.energyMax by 25).foreach(energy => {
        val x = xStart + width * energy / unit.energyMax
        DrawMap.line(Pixel(x, yStartEnergy), Pixel(x, yEndEnergy), Color.Black)
      })
      DrawMap.box(Pixel(xStart, yStartEnergy), Pixel(xStart + widthEnergyNow, yEndEnergy), Colors.BrightTeal, solid = true)
    }
    
    if (unit.complete && widthCooldownNow > 0) {
      DrawMap.box(Pixel(xStart - 1, yStartCooldown - 1), Pixel(xStart + width + 2, yEndCooldown + 1), Color.Black, solid = true)
      if (unit.cooldownLeft > 0) {
        DrawMap.box(Pixel(xStartCooldown, yStartCooldown), Pixel(xStartCooldown + widthCooldownNow, yEndCooldown), unit.teamColor, solid = true)
      }
    }
  }
}
