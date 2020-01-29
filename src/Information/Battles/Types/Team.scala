package Information.Battles.Types

import Mathematics.Points.{Pixel, SpecificPoints}
import Mathematics.PurpleMath
import Performance.Cache
import ProxyBwapi.UnitInfo.UnitInfo

class Team(val units: Vector[UnitInfo]) {
  
  //////////////////////////////////////////////
  // Populate immediately after construction! //
  //////////////////////////////////////////////
  
  var battle      : Battle  = _
  var vanguard    : Pixel   = SpecificPoints.middle
  var centroid    : Pixel   = SpecificPoints.middle
  
  //////////////
  // Features //
  //////////////
  
  def opponent: Team = if (battle.us == this) battle.enemy else battle.us

  lazy val meanDamageGround = new Cache(() => PurpleMath.nanToZero(PurpleMath.weightedMean(units.map(u => (u.damageOnHitGround  .toDouble,  u.dpfGround)))))
  lazy val meanDamageAir    = new Cache(() => PurpleMath.nanToZero(PurpleMath.weightedMean(units.map(u => (u.damageOnHitAir     .toDouble,  u.dpfAir)))))
  def meanDamageAgainst(unit: UnitInfo): Double = if (unit.flying) meanDamageAir() else meanDamageGround()


}
