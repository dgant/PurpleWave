package Micro.Heuristics

import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo

object MicroValue {
  
  val gasToMineralsRatio = 1.5
  
  def valuePerDamageMaxHp(unit: UnitInfo): Double = {
    Maff.nanToZero(unit.subjectiveValue / unit.totalHealth.toDouble)
  }
  
  def valuePerDamageCurrentHp(unit: UnitInfo): Double = {
    Maff.nanToZero(unit.subjectiveValue / unit.totalHealth.toDouble)
  }
  
  def valuePerAttackMaxHp(from: UnitInfo, to: UnitInfo): Double = {
    from.damageOnNextHitAgainst(to) * valuePerDamageMaxHp(to)
  }
  
  def valuePerAttackCurrentHp(from: UnitInfo, to: UnitInfo): Double = {
    from.damageOnNextHitAgainst(to) * valuePerDamageCurrentHp(to)
  }
  
  def valuePerFrameMaxHp(from: UnitInfo, to: UnitInfo): Double = {
    Maff.nanToOne(valuePerAttackMaxHp(from, to) / from.cooldownMaxAgainst(to))
  }
  
  def valuePerFrameCurrentHp(from: UnitInfo, to: UnitInfo): Double = {
    Maff.nanToOne(valuePerAttackCurrentHp(from, to) / from.cooldownMaxAgainst(to))
  }
  
  def valuePerFrameRepairing(target: UnitInfo): Double = {
    val valuePerDamage = MicroValue.valuePerDamageMaxHp(target)
    val damagePerFrame = Maff.nanToZero(0.9 * target.unitClass.maxHitPoints / target.unitClass.buildFrames)
    val valuePerFrame = valuePerDamage * damagePerFrame
    valuePerFrame
  }
}
