package Micro.Decisions

import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo

object MicroValue {
  
  def valuePerDamage(unit: UnitInfo): Double = {
    unit.unitClass.subjectiveValue / unit.totalHealth
  }
  
  def valuePerAttack(from: UnitInfo, to: UnitInfo): Double = {
    from.damageAgainst(to) * valuePerDamage(to)
  }
  
  def valuePerFrame(from: UnitInfo, to: UnitInfo): Double = {
    PurpleMath.nanToOne(from.damageAgainst(to) * valuePerDamage(to) / from.cooldownMaxAgainst(to))
  }
}
