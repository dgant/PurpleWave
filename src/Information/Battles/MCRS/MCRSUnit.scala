package Information.Battles.MCRS

import Lifecycle.With
import Performance.Cache
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class MCRSUnit(val unit: UnitInfo) {

  val target = new Cache(() =>
    unit.friendly.flatMap(_.agent.toAttack)
    .orElse(unit.orderTarget.filter(_.isEnemyOf(unit)))
    .orElse(ByOption.minBy(unit.matchups.targets)(t => t.pixelDistanceEdge(unit) + (if (t.canAttack || t.unitClass.spells.nonEmpty) 32 * 15 else 0))))
  val engagePosition = new Cache(() => target().map(unit.pixelToFireAt).getOrElse(unit.pixelCenter))
  val sim = new Cache(() => MCRSim.getSimValue(unit))

  def percentHealth: Double = MCRSMath.percentHealth(unit)
  val visibleGroundStrength = new Cache(() => MCRSMath.visGroundStrength(unit))
  val visibleAirStrength = new Cache(() => MCRSMath.visAirStrength(unit))
  val maxGroundStrength = new Cache(() => MCRSMath.maxGroundStrength(unit))
  val maxAirStrength = new Cache(() => MCRSMath.maxAirStrength(unit))

  def fightThreshold: Double = With.blackboard.aggressionRatio() * (if (With.enemy.isTerran) .75 else 1.0)
  def shouldFight: Boolean = sim().simValue >= fightThreshold
}
