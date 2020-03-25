package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Color

object ShowHealthAndCooldown extends View {
  
  override def renderMap() { With.units.all.foreach(renderUnit) }
  
  def renderUnit(unit: UnitInfo) {
    if ( ! With.viewport.contains(unit.pixelCenter)) return
    if ( ! unit.likelyStillAlive) return
    if ( ! unit.possiblyStillThere) return
    if (unit.invincible) return
    
    val width = Math.min(48, Math.max(18, (unit.unitClass.maxTotalHealth + unit.defensiveMatrixPoints) / 5))
    val marginTopHp = 3
    val denominator = unit.unitClass.maxTotalHealth + (if (unit.defensiveMatrixPoints > 0) width * 250 else 0)
    
    if (denominator == 0) return
    
    val widthHpMax = width * unit.unitClass.maxHitPoints / denominator
    val widthShMax = width * unit.unitClass.maxShields / denominator
    val widthDmMax = if (unit.defensiveMatrixPoints > 0) width * 250 / denominator else 0
    val widthHpNow = width * unit.hitPoints / denominator
    val widthShNow = width * unit.shieldPoints / denominator
    val widthDmNow = width * unit.defensiveMatrixPoints / denominator
    val widthEnergyNow = if (unit.energyMax == 0) 0 else Math.min(width, width * unit.energy / unit.energyMax)

    // Min, because I haven't yet accounted for energy max upgrades
    val widthCooldownButton = 3
    val widthCooldown = width - 2 * widthCooldownButton - 2
    val widthCooldownNow = widthCooldown * Math.max(unit.cooldownLeft, unit.spellCooldownLeft) / Math.max(1, unit.cooldownMaxAirGround) //TODO: Max spell cooldown?
    
    val yStartHp = unit.pixelCenter.y + unit.unitClass.height / 2 - marginTopHp
    val yEndHp = yStartHp + 4
    val yStartEnergy = yEndHp + 2
    val yEndEnergy = yStartEnergy + 4
    val yStartCooldown = if (unit.energyMax > 0) yEndEnergy + 3 else yEndHp + 3
    val yEndCooldown = yStartCooldown + 3
    val xStart = unit.pixelCenter.x - width / 2
    val xStartSh = xStart + widthHpMax
    val xStartDm = xStartSh + widthShMax
    val xStartCooldown = xStart
    val xStartCooldownButton0 = xStart + widthCooldown + 1
    val xStartCooldownButton1 = xStart + widthCooldown + 2 + widthCooldownButton
    
    val colorHp = Colors.NeonGreen
    val colorSh = Colors.NeonBlue
    val colorDm = Colors.NeonViolet
    
    if (unit.totalHealth != unit.unitClass.maxTotalHealth) {
      DrawMap.box(Pixel(xStart - 1, yStartHp - 1), Pixel(xStart + width + 2, yEndHp + 1), Color.Black, solid = true)
      DrawMap.box(Pixel(xStartDm, yStartHp), Pixel(xStartDm + widthDmMax, yEndHp), colorDm, solid = false)
      DrawMap.box(Pixel(xStartSh, yStartHp), Pixel(xStartSh + widthShMax, yEndHp), colorSh, solid = false)
      DrawMap.box(Pixel(xStart, yStartHp), Pixel(xStart + widthHpMax, yEndHp), colorHp, solid = false)
      DrawMap.box(Pixel(xStartDm, yStartHp), Pixel(xStartDm + widthDmNow, yEndHp), colorDm, solid = true)
      DrawMap.box(Pixel(xStartSh, yStartHp), Pixel(xStartSh + widthShNow, yEndHp), colorSh, solid = true)
      DrawMap.box(Pixel(xStart, yStartHp), Pixel(xStart + widthHpNow, yEndHp), colorHp, solid = true)
  
      val healthBarEvery = Math.max(3, width * 25 / (unit.unitClass.maxTotalHealth + unit.defensiveMatrixPoints))
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
        DrawMap.box(Pixel(xStartCooldown, yStartCooldown), Pixel(xStartCooldown + widthCooldownNow, yEndCooldown), unit.color, solid = true)
      }
      if (unit.attackStarting) {
        DrawMap.box(Pixel(xStartCooldownButton1, yStartCooldown), Pixel(xStartCooldownButton1 + widthCooldownButton, yEndCooldown), Colors.BrightGreen, solid = true)
      }
      if (unit.attackAnimationHappening) {
        DrawMap.box(Pixel(xStartCooldownButton0, yStartCooldown), Pixel(xStartCooldownButton0 + widthCooldownButton, yEndCooldown), Colors.BrightOrange, solid = true)
      }
    }
  }
}
