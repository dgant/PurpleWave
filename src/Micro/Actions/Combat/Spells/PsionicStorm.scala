package Micro.Actions.Combat.Spells

import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object PsionicStorm extends TargetedSpell {
  
  override protected def casterClass    : UnitClass = Protoss.HighTemplar
  override protected def tech           : Tech      = Protoss.PsionicStorm
  override protected def aoe            : Boolean   = true
  override protected def castRangeTiles : Int       = 9
  override protected def thresholdValue : Double    = casterClass.subjectiveValue / 2.5
  override protected def frameDuration  : Double    = 18
  
  override protected def valueTarget(target: UnitInfo): Double = {
    if (With.grids.psionicStorm.isSet(target.tileIncludingCenter)) return 0.0
    if (target.unitClass.isBuilding)  return 0.0
    if (target.underStorm)            return 0.0
    if (target.invincible)            return 0.0
    if (target.is(Zerg.Larva))        return 0.0
    if (target.is(Zerg.Egg))          return 0.0
    if (target.is(Zerg.LurkerEgg))    return 0.0
    
    val output = (
      target.subjectiveValue *
      (Math.min(112.0, target.totalHealth) / target.unitClass.maxTotalHealth) *
      (if (target.moving) 0.7 else 1.0) *
      (if (target.isEnemy) 1.0 else -4.0)
    )
    
    output
  }
  
  override def onCast(caster: FriendlyUnitInfo, target: Pixel) {
    With.grids.psionicStorm.addPsionicStorm(target)
  }
}
