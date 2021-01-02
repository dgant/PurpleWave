package Micro.Actions.Combat.Spells

import Lifecycle.With
import Mathematics.Points.TileRectangle
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Irradiate extends TargetedSpell {

  override protected def casterClass    : UnitClass = Terran.ScienceVessel
  override protected def tech           : Tech      = Terran.Irradiate
  override protected def aoe            : Boolean   = false
  override protected def castRangeTiles : Int       = 9
  override protected def thresholdValue : Double    = Zerg.Mutalisk.subjectiveValue

  protected def valueUnit(target: UnitInfo): Double = {
    if ( ! target.unitClass.canBeIrradiateBurned)
      0.0
    else if (target.irradiated)
      0.0
    else if (target.is(Zerg.LurkerEgg))
      0.0
    else if (target.isEnemy)
      target.subjectiveValue
    else if (target.isFriendly)
      - target.subjectiveValue
    else
      0.0
  }

  override protected def valueTarget(target: UnitInfo, caster: FriendlyUnitInfo): Double = {
    val targetValue = valueUnit(target) * target.hitPoints / target.unitClass.maxHitPoints
    val neighborValue = With.units.inTileRectangle(TileRectangle(
      target.tile.subtract(1, 1),
      target.tile.add(1, 1))).view.map(valueUnit).sum

    targetValue + neighborValue / 2.0
  }
}
