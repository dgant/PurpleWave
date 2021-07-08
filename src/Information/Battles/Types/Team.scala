package Information.Battles.Types

import Lifecycle.With
import Mathematics.Maff
import Performance.Cache
import ProxyBwapi.UnitInfo.UnitInfo
import Tactics.Squads.GroupConsensus

class Team(val battle: Battle, val units: Vector[UnitInfo]) extends GroupConsensus {

  final protected def consensusUnits: Seq[UnitInfo] = units

  lazy val us         : Boolean       = this == battle.us
  lazy val enemy      : Boolean       = this == battle.enemy
  lazy val opponent   : Team          = if (us) battle.enemy else battle.us
  def attackers       : Seq[UnitInfo] = units.view.filter(u => u.unitClass.canAttack  && ! u.unitClass.isWorker )
  val hasCatchersAir    = new Cache(() => attackers.exists(a => (a.pixelRangeAir > 96 || a.flying) && a.canAttackAir))
  val hasCatchersGround = new Cache(() => attackers.exists(a => (a.pixelRangeAir > 96 || a.flying) && a.canAttackGround))
  val vanguard = new Cache(() =>
    Maff.minBy(attackers)(_.pixelDistanceSquared(opponent.centroidKey))
    .orElse(Maff.minBy(units)(_.pixelDistanceSquared(opponent.centroidKey)))
    .map(_.pixel)
    .getOrElse(With.scouting.threatOrigin.center))
}
