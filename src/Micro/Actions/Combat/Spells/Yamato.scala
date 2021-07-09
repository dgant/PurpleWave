package Micro.Actions.Combat.Spells

import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo._

object Yamato extends TargetedSpell {
  
  override protected def casterClass    : UnitClass = Terran.Battlecruiser
  override protected def tech           : Tech      = Terran.Yamato
  override protected def aoe            : Boolean   = false
  override protected def castRangeTiles : Int       = 10
  override protected def thresholdValue : Double    = Math.min(Terran.Goliath.subjectiveValue, Protoss.Shuttle.subjectiveValue)
  
  override protected def valueTarget(target: UnitInfo, caster: FriendlyUnitInfo): Double = {
    if ( ! target.isEnemy) return 0.0
  
    val output = target.subjectiveValue / Math.max(1.0, caster.pixelDistanceEdge(target) - 32 * castRangeTiles - caster.matchups.pixelsOfSafety)
    output
  }
}
