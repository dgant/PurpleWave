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
  val survivability = new Cache(() => MCRSMath.survivability(unit))
  val dpsGround = new Cache(() => MCRSMath.dpfGround(unit))
  val dpsAir = new Cache(() => MCRSMath.dpfAir(unit))
  val strengthGround = new Cache(() => MCRSMath.strengthGround(unit))
  val strengthAir = new Cache(() => MCRSMath.strengthAir(unit))

  def fightThreshold: Double = With.blackboard.aggressionRatio() * (if (With.enemy.isTerran) .75 else 0.9)
  def shouldFight: Boolean = sim().simValue >= fightThreshold
}
