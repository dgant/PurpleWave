package Micro.Decisions

import Mathematics.PurpleMath
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo

object MicroValue {
  
  def valuePerDamage(unit: UnitInfo): Double = {
    PurpleMath.nanToZero(unit.subjectiveValue / unit.totalHealth.toDouble)
  }
  
  def valuePerAttack(from: UnitInfo, to: UnitInfo): Double = {
    from.damageOnNextHitAgainst(to) * valuePerDamage(to)
  }
  
  def valuePerFrame(from: UnitInfo, to: UnitInfo): Double = {
    PurpleMath.nanToOne(from.damageOnNextHitAgainst(to) * valuePerDamage(to) / from.cooldownMaxAgainst(to))
  }
  
  def maxSplashFactor(unit: UnitInfo): Double = {
    if(unit.unitClass.dealsRadialSplashDamage || unit.is(Zerg.Lurker))
      2.0
    else if(unit.is(Zerg.Mutalisk))
      1.5
    else
      1.0
  }
}
