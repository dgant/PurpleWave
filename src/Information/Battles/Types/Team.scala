package Information.Battles.Types

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Agency.AnchorMargin
import Performance.Cache
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class Team(val units: Vector[UnitInfo]) {

  //////////////////////////////////////////////
  // Populate immediately after construction! //
  //////////////////////////////////////////////
  
  var battle: Battle  = _

  //////////////
  // Features //
  //////////////

  lazy val us: Boolean = this == battle.us
  lazy val enemy: Boolean = this == battle.enemy
  lazy val opponent: Team = if (us) battle.enemy else battle.us
  lazy val zones: Set[Zone] = units.map(_.zone).toSet
  def attackers       : Seq[UnitInfo] = units.view.filter(u => u.unitClass.canAttack  && ! u.unitClass.isWorker )
  def attackersGround : Seq[UnitInfo] = attackers.view.filterNot(_.flying)
  val hasGround = new Cache(() => attackersGround.nonEmpty)
  val centroidAir = new Cache(() => GroupCentroid.air(attackers))
  val centroidGround = new Cache(() => GroupCentroid.ground(attackers))
  def centroidOf(unit: UnitInfo): Pixel = if (unit.flying) centroidAir() else centroidGround()
  val vanguard = new Cache(() =>
    ByOption.minBy(attackers)(_.pixelDistanceSquared(opponent.centroidAir()))
    .orElse(ByOption.minBy(units)(_.pixelDistanceSquared(opponent.centroidAir())))
    .map(_.pixel)
    .getOrElse(With.scouting.threatOrigin.pixelCenter))
  val anchorMargin = new Cache(() => AnchorMargin.marginOf(units.view.flatMap(_.friendly)))

  // Used by MCRS
  private lazy val meanDamageGround = new Cache(() => PurpleMath.nanToZero(PurpleMath.weightedMean(units.map(u => (u.damageOnHitGround  .toDouble,  u.dpfGround)))))
  private lazy val meanDamageAir    = new Cache(() => PurpleMath.nanToZero(PurpleMath.weightedMean(units.map(u => (u.damageOnHitAir     .toDouble,  u.dpfAir)))))
  def meanDamageAgainst(unit: UnitInfo): Double = if (unit.flying) meanDamageAir() else meanDamageGround()

  val engaged   = new Cache(() => units.exists(_.matchups.framesOfSafety <= 0))
  val axisDepth = new Cache(() => centroidAir().radiansTo(opponent.centroidAir()))
  val axisWidth = new Cache(() => PurpleMath.normalizeAroundZero(axisDepth() + Math.PI / 2))
  val lineDepth = new Cache(() => centroidGround().radiateRadians(axisDepth(), With.mapPixelPerimeter))
  val lineWidth = new Cache(() => centroidGround().radiateRadians(axisWidth(), With.mapPixelPerimeter))

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
  val widthOrder          = new Cache(() => attackersGround.sortBy(_.widthSlotProjected().pixelDistanceSquared(lineWidth())).toVector)
  val widthIdeal          = new Cache(() => attackersGround.map(_.unitClass.radialHypotenuse * 2.5).sum) // x2.5 = x2 for diameter, then x1.25 for spacing
  val widthMeanExpected   = new Cache(() => widthIdeal() / 2)
  val widthMeanActual     = new Cache(() => PurpleMath.mean(units.view.flatMap(_.widthContribution())))
  val depthMean           = new Cache(() => ByOption.mean(units.view.flatMap(_.depthCurrent())).getOrElse(0d))
  val depthSpread         = new Cache(() => ByOption.mean(units.view.flatMap(_.depthCurrent()).map(d => Math.abs(d - depthMean()))).getOrElse(0d))
  val coherenceWidth      = new Cache(() => if (units.size == 1) 1 else Math.min(PurpleMath.nanToOne(widthMeanExpected() / widthMeanActual()), PurpleMath.nanToOne(widthMeanActual() / widthMeanExpected())))
  val coherenceDepth      = new Cache(() => 1 - PurpleMath.clamp(2 * depthSpread() / (128 + widthIdeal()), 0, 1))
  val coherence           = new Cache(() => coherenceDepth()) // Math.max(coherenceWidth(), coherenceDepth()))
  val impatience          = new Cache(() => units.view.flatMap(_.friendly.map(_.agent.impatience)).sum.toDouble / Math.max(1, units.size))
  val totalArmyFraction   = new Cache(() => units.view.filter(MatchWarriors).map(_.subjectiveValue).sum / Math.max(1d, With.units.ours.view.filter(MatchWarriors).map(_.subjectiveValue).sum))
}
