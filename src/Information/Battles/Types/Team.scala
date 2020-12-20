package Information.Battles.Types

import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints}
import Mathematics.PurpleMath
import Performance.Cache
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class Team(val units: Vector[UnitInfo]) {
  
  //////////////////////////////////////////////
  // Populate immediately after construction! //
  //////////////////////////////////////////////
  
  var battle          : Battle  = _
  var vanguard        : Pixel   = SpecificPoints.middle
  var centroidAir     : Pixel   = SpecificPoints.middle
  var centroidGround  : Pixel   = if (With.frame == 0) centroidAir else SpecificPoints.middle.nearestWalkableTile.pixelCenter // Hack fix to startup initialization order

  //////////////
  // Features //
  //////////////
  
  lazy val opponent: Team = if (battle.us == this) battle.enemy else battle.us
  def centroidOf(unit: UnitInfo): Pixel = if (unit.flying) centroidAir else centroidGround

  // Used by MCRS
  private lazy val meanDamageGround = new Cache(() => PurpleMath.nanToZero(PurpleMath.weightedMean(units.map(u => (u.damageOnHitGround  .toDouble,  u.dpfGround)))))
  private lazy val meanDamageAir    = new Cache(() => PurpleMath.nanToZero(PurpleMath.weightedMean(units.map(u => (u.damageOnHitAir     .toDouble,  u.dpfAir)))))
  def meanDamageAgainst(unit: UnitInfo): Double = if (unit.flying) meanDamageAir() else meanDamageGround()

  val engaged = new Cache(() => units.exists(_.matchups.framesOfSafety <= 0))
  val axisDepth = new Cache(() => centroidAir.radiansTo(opponent.centroidAir))
  val axisWidth = new Cache(() => PurpleMath.normalizeAroundZero(axisDepth() + Math.PI / 2))
  val lineDepth = new Cache(() => centroidGround.radiateRadians(axisDepth(), With.mapPixelPerimeter))
  val lineWidth = new Cache(() => centroidGround.radiateRadians(axisWidth(), With.mapPixelPerimeter))

  // COHERENCE: A metric of how organized the team is.
  //
  // A fully (1.0) organized team is a few-rank arc equidistant from enemy targets.
  // The less organized the team is, the more attractive it is to reorganize.
  //
  // Aspects of coherence:
  // - Depth: Average distance from target (as a delta from each unit's ideal)
  // - Width: Distance from the depth axis (compared to expectation based on size of team)
  //
  // For elements on a line, mean distance from center scales with scale of number of units.
  // Example:
  //   101     -> Width=3 SumDistance=2
  //   21012   -> Width=5 SumDistance=6
  //   3210123 -> Width=5 SumDistance=12
  // SumDistance = 0 1 2 4 6 9 12 16 20 25 30 36 42 49 56
  // Width / 2   = 0 1 1 2 2 3 3  4  4  5  5  6  6  7  7
  // Width       = 1 2 3 4 5 6 7  8  9  10 11 12 13 14 15
  // Thus "width" = 2 * sqrt(sumDistance)
  private def groundCombatUnits = units.view.filter(u => u.canMove && u.unitClass.attacksOrCastsOrDetectsOrTransports && ! u.flying)
  val widthOrder          = new Cache(() => groundCombatUnits.sortBy(_.positioningWidthCurrentCached().pixelDistanceSquared(lineWidth())).toVector)
  val widthIdeal          = new Cache(() => groundCombatUnits.map(_.unitClass.radialHypotenuse * 3).sum) // x3 = x2 for diameter, then x1.5 for spacing
  val widthMeanExpected   = new Cache(() => widthIdeal() / 2)
  val widthMeanActual     = new Cache(() => PurpleMath.mean(units.view.map(_.positioningWidthCached())))
  val depthMean           = new Cache(() => ByOption.mean(units.view.flatMap(_.positioningDepthCached())).getOrElse(0d))
  val depthSpread         = new Cache(() => ByOption.mean(units.view.flatMap(_.positioningDepthCached()).map(d => Math.abs(d - depthMean()))).getOrElse(0d))
  val depthSpreadExpected = new Cache(() => units.size / 8) // Magic number
  val coherenceWidth      = new Cache(() => if (units.size == 1) 1 else Math.min(PurpleMath.nanToOne(widthMeanExpected() / widthMeanActual()), PurpleMath.nanToOne(widthMeanActual() / widthMeanExpected())))
  val coherenceDepth      = new Cache(() => PurpleMath.nanToOne(1 - depthSpread() / (depthSpread() + depthSpreadExpected())))
  val coherence           = new Cache(() => Math.max(coherenceWidth(), coherenceDepth()))
  val impatience          = new Cache(() => units.view.flatMap(_.friendly.map(_.agent.impatience)).sum.toDouble / Math.max(1, units.size))
  val totalArmyFraction   = new Cache(() => units.view.filter(_.is(UnitMatchWarriors)).map(_.subjectiveValue).sum / Math.max(1d, With.units.ours.view.filter(_.is(UnitMatchWarriors)).map(_.subjectiveValue).sum))
}
