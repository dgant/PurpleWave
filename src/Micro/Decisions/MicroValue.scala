package Micro.Decisions

import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo

object MicroValue {
  
  def valuePerDamage(unit: UnitInfo): Double = {
    PurpleMath.nanToZero(unit.unitClass.subjectiveValue / unit.totalHealth.toDouble)
  }
  
  def valuePerAttack(from: UnitInfo, to: UnitInfo): Double = {
    from.damageOnNextHitAgainst(to) * valuePerDamage(to)
  }
  
  def valuePerFrame(from: UnitInfo, to: UnitInfo): Double = {
    PurpleMath.nanToOne(from.damageOnNextHitAgainst(to) * valuePerDamage(to) / from.cooldownMaxAgainst(to))
  }
}
