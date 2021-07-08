package Information.Battles.Types

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Maff
import Performance.Cache
import ProxyBwapi.UnitInfo.UnitInfo
import Tactics.Squads.GroupConsensus

class Team(val units: Vector[UnitInfo]) extends GroupConsensus {

  //////////////////////////////////////////////
  // Populate immediately after construction! //
  //////////////////////////////////////////////
  
  var battle: Battle  = _

  //////////////
  // Features //
  //////////////

  final protected def consensusUnits: Seq[UnitInfo] = units

  lazy val us         : Boolean       = this == battle.us
  lazy val enemy      : Boolean       = this == battle.enemy
  lazy val opponent   : Team          = if (us) battle.enemy else battle.us
  lazy val zones      : Set[Zone]     = units.map(_.zone).toSet
  def attackers       : Seq[UnitInfo] = units.view.filter(u => u.unitClass.canAttack  && ! u.unitClass.isWorker )
  def attackersGround : Seq[UnitInfo] = attackers.view.filterNot(_.flying)
  val hasCatchersAir    = new Cache(() => attackers.exists(a => (a.pixelRangeAir > 96 || a.flying) && a.canAttackAir))
  val hasCatchersGround = new Cache(() => attackers.exists(a => (a.pixelRangeAir > 96 || a.flying) && a.canAttackGround))
  val vanguard = new Cache(() =>
    Maff.minBy(attackers)(_.pixelDistanceSquared(opponent.centroidKey))
    .orElse(Maff.minBy(units)(_.pixelDistanceSquared(opponent.centroidKey)))
    .map(_.pixel)
    .getOrElse(With.scouting.threatOrigin.center))


  // Used by MCRS
  private lazy val meanDamageGround = new Cache(() => Maff.nanToZero(Maff.weightedMean(units.map(u => (u.damageOnHitGround  .toDouble,  u.dpfGround)))))
  private lazy val meanDamageAir    = new Cache(() => Maff.nanToZero(Maff.weightedMean(units.map(u => (u.damageOnHitAir     .toDouble,  u.dpfAir)))))
  def meanDamageAgainst(unit: UnitInfo): Double = if (unit.flying) meanDamageAir() else meanDamageGround()
}
