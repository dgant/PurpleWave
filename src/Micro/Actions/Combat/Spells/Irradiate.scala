package Micro.Actions.Combat.Spells

import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Irradiate extends TargetedSpell {

  override protected def casterClass    : UnitClass = Terran.ScienceVessel
  override protected def tech           : Tech      = Terran.Irradiate
  override protected def aoe            : Boolean   = false
  override protected def castRangeTiles : Int       = 9
  override protected def thresholdValue : Double    = Zerg.Lurker.subjectiveValue

  protected def valueUnit(target: UnitInfo): Double = {
          if ( ! target.unitClass.canBeIrradiateBurned) 0.0
    else  if (target.irradiated)                        0.0
    else  if (Zerg.LurkerEgg(target))                   0.0
    else if (target.isEnemy)                            target.subjectiveValue
    else if (target.isFriendly)                       - target.subjectiveValue
    else                                                0.0
  }

  override protected def valueTarget(target: UnitInfo, caster: FriendlyUnitInfo): Double = {
    val targetValue = valueUnit(target) * target.hitPoints / target.unitClass.maxHitPoints

    if (targetValue <= 0) {
      return targetValue
    }

    val neighborValue = target.tile.toRectangle.expand(1).tiles.flatMap(_.units.view).map(valueUnit).sum
    val casts = caster.energy / Terran.Irradiate.energyCost

    casts * (targetValue + 0.5 * neighborValue)
  }
}
