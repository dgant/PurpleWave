package Micro.Actions.Combat.Spells

import Mathematics.Maff
import ProxyBwapi.Races.Protoss
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?

object Maelstrom extends TargetedSpell {
  
  override protected def casterClass      : UnitClass = Protoss.DarkArchon
  override protected def tech             : Tech      = Protoss.Maelstrom
  override protected def aoe              : Boolean   = true
  override protected def castRangeTiles   : Int       = 10
  override protected def thresholdValue   : Double    = 1.0
  override protected def lookaheadPixels  : Int       = 6
  override protected def additionalConditions(unit: FriendlyUnitInfo): Boolean = unit.agent.shouldFight || unit.matchups.threatsInRange.nonEmpty

  val castFrames = 1

  override protected def valueTarget(target: UnitInfo, caster: FriendlyUnitInfo): Double = {
    if ( ! target.unitClass.canBeMaelstrommed) return 0.0
    if ( ! target.visible)  return 0.0
    if (target.stasised)    return 0.0
    if (target.invincible)  return 0.0

    val multiplierConfidence  = Math.max(1.0, Maff.nanToOne(1 / (1 + caster.confidence11)))
    val multiplierPlayer      = ?(target.isEnemy, 1.0, ?(target.isFriendly, -1.0, 0.0))
    val multiplierUnit        = target.unitClass.maelstromValue
    val multiplierDanger      = 1.0 + 0.5 * ?(caster.matchups.threatsInRange.nonEmpty, caster.unitClass.maxTotalHealth / caster.totalHealth, 0)
    val multiplierRich        = 0.5 + 0.5 * caster.team.map(_.maelstromCount).getOrElse(1)
    val output                = multiplierConfidence * multiplierPlayer * multiplierUnit * multiplierDanger * multiplierRich
    output
  }
}
