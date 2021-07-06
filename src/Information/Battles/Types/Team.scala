package Information.Battles.Types

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Performance.Cache
import ProxyBwapi.UnitInfo.UnitInfo

class Team(val units: Vector[UnitInfo]) {

  //////////////////////////////////////////////
  // Populate immediately after construction! //
  //////////////////////////////////////////////
  
  var battle: Battle  = _

  //////////////
  // Features //
  //////////////

  lazy val us         : Boolean       = this == battle.us
  lazy val enemy      : Boolean       = this == battle.enemy
  lazy val opponent   : Team          = if (us) battle.enemy else battle.us
  lazy val zones      : Set[Zone]     = units.map(_.zone).toSet
  def attackers       : Seq[UnitInfo] = units.view.filter(u => u.unitClass.canAttack  && ! u.unitClass.isWorker )
  def attackersGround : Seq[UnitInfo] = attackers.view.filterNot(_.flying)
  val engaged           = new Cache(() => units.exists(_.matchups.pixelsOfEntanglement >= 0))
  val hasGround         = new Cache(() => attackersGround.nonEmpty)
  val hasCatchersAir    = new Cache(() => attackers.exists(a => (a.pixelRangeAir > 96 || a.flying) && a.canAttackAir))
  val hasCatchersGround = new Cache(() => attackers.exists(a => (a.pixelRangeAir > 96 || a.flying) && a.canAttackGround))
  val centroidAir       = new Cache(() => GroupCentroid.air(attackers))
  val centroidGround    = new Cache(() => GroupCentroid.ground(attackers))
  def centroidOf(unit: UnitInfo): Pixel = if (unit.flying) centroidAir() else centroidGround()
  val vanguard = new Cache(() =>
    Maff.minBy(attackers)(_.pixelDistanceSquared(opponent.centroidAir()))
    .orElse(Maff.minBy(units)(_.pixelDistanceSquared(opponent.centroidAir())))
    .map(_.pixel)
    .getOrElse(With.scouting.threatOrigin.center))


  // Used by MCRS
  private lazy val meanDamageGround = new Cache(() => Maff.nanToZero(Maff.weightedMean(units.map(u => (u.damageOnHitGround  .toDouble,  u.dpfGround)))))
  private lazy val meanDamageAir    = new Cache(() => Maff.nanToZero(Maff.weightedMean(units.map(u => (u.damageOnHitAir     .toDouble,  u.dpfAir)))))
  def meanDamageAgainst(unit: UnitInfo): Double = if (unit.flying) meanDamageAir() else meanDamageGround()
}
