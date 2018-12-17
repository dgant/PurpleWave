package Micro.Actions.Combat.Spells

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object PsionicStorm extends TargetedSpell {
  
  override protected def casterClass      : UnitClass = Protoss.HighTemplar
  override protected def tech             : Tech      = Protoss.PsionicStorm
  override protected def aoe              : Boolean   = true
  override protected def castRangeTiles   : Int       = 9
  override protected def thresholdValue   : Double    = casterClass.subjectiveValue / 4.0
  override protected def lookaheadFrames  : Int       = 6 + With.latency.latencyFrames

  override protected def valueTarget(target: UnitInfo): Double = {
    if (With.grids.psionicStorm.isSet(target.tileIncludingCenter)) return 0.0
    if (target.unitClass.isBuilding)    return 0.0
    if (target.underStorm)              return 0.0
    if (target.stasised)                return 0.0
    if (target.invincible)              return 0.0
    if (target.isAny(
      Protoss.Interceptor,
      Zerg.Larva,
      Zerg.Egg,
      Zerg.LurkerEgg)) return 0.0

    val expectedAccuracy  = PurpleMath.clamp(Terran.Marine.topSpeed / target.topSpeed, 0.4, 1.0)
    val multiplierValue   = Math.min(target.subjectiveValue, Protoss.Observer.subjectiveValue)
    val multiplierDamage  = (Math.min(expectedAccuracy * 112.0, target.totalHealth) / target.unitClass.maxTotalHealth)
    val multiplierPlayer  = (if (target.isEnemy) 1.0 else -3.0)
    val output = (
      multiplierValue
      * multiplierDamage
      * multiplierPlayer)
    
    output
  }
  
  override def onCast(caster: FriendlyUnitInfo, target: Pixel) {
    With.grids.psionicStorm.addPsionicStorm(target)
  }
}
