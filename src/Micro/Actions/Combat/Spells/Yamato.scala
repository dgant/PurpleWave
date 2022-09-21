package Micro.Actions.Combat.Spells

import ProxyBwapi.Races.Terran
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo._

object Yamato extends TargetedSpell {
  
  override protected def casterClass    : UnitClass = Terran.Battlecruiser
  override protected def tech           : Tech      = Terran.Yamato
  override protected def aoe            : Boolean   = false
  override protected def castRangeTiles : Int       = 10
  override protected def thresholdValue : Double    = Terran.Goliath.subjectiveValue

  override protected def valueTarget(target: UnitInfo, caster: FriendlyUnitInfo): Double = {
    if ( ! target.isEnemy) return 0.0

    val reach = caster.pixelDistanceEdge(target) - 32 * castRangeTiles
    val output = target.subjectiveValue / Math.max(1.0, reach + caster.matchups.pixelsEntangled)
    output
  }
}
