package Micro.Matchups

import Information.Battles.BattleClassificationFilters
import Lifecycle.With
import Mathematics.Maff
import Performance.Cache
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo
import Tactics.Squads.{GenericUnitGroup, UnitGroup}

case class MatchupAnalysis(me: UnitInfo) {
  // Default units allow identification of targets when destroying an empty base, because no Battle is happening
  // The necessity of this is a good argument for defining battles even if they would have trivial simulation results
  private def defaultUnits: Seq[UnitInfo] = if (me.canAttack) me.zone.units.view.filter(u => u.isEnemy && BattleClassificationFilters.isEligibleLocal(u)) else Seq.empty.view

  def groupUs: UnitGroup = _groupUs()
  def groupEnemy: UnitGroup = _groupEnemy()
  private val _groupUs = new Cache(() => me.team.orElse(me.friendly.flatMap(_.squad)).getOrElse(GenericUnitGroup(Seq(me))))
  private val _groupEnemy = new Cache(() => me.team.map(_.opponent)
    .orElse(me.friendly.flatMap(_.squad.flatMap(_.targetQueue.map(GenericUnitGroup))))
    .orElse(me.friendly.flatMap(_.squad.flatMap(s => Some(s.enemies).filter(_.nonEmpty).map(GenericUnitGroup))))
    .getOrElse(GenericUnitGroup(defaultUnits)))

  private def battleAll     : Option[Seq[UnitInfo]] = me.battle.map(_.teams.view.flatMap(_.units))
  private def battleUs      : Option[Seq[UnitInfo]] = me.team.map(_.units.view)
  private def battleEnemies : Option[Seq[UnitInfo]] = me.team.map(_.opponent.units.view)
  private def entrants      : Seq[UnitInfo]         = me.battle.flatMap(With.matchups.entrants.get).getOrElse(Seq.empty).view

  private def withEntrants(source: Seq[UnitInfo], filter: (UnitInfo) => Boolean = (unit) => true): Seq[UnitInfo] = source ++ entrants.filter(filter).filterNot(source.contains)

  def alliesInclSelf          : Seq[UnitInfo] = battleUs.map(withEntrants(_, _.isFriendly)).getOrElse(groupUs.groupUnits.view)
  def allies                  : Seq[UnitInfo] = alliesInclSelf.filterNot(_.id == me.id)
  def enemies                 : Seq[UnitInfo] = battleEnemies.map(withEntrants(_, _.isEnemy)).getOrElse(groupEnemy.groupUnits.view)
  def others                  : Seq[UnitInfo] = enemies ++ allies
  def allUnits                : Seq[UnitInfo] = others :+ me
  def enemyDetectors          : Seq[UnitInfo] = groupEnemy.detectors.view
  def threats                 : Seq[UnitInfo] = enemies.filter(threatens(_, me)).filterNot(Protoss.Interceptor)
  def targets                 : Seq[UnitInfo] = enemies.filter(threatens(me, _))
  def threatsInRange          : Seq[UnitInfo] = threats.filter(threat => threat.pixelRangeAgainst(me) >= threat.pixelDistanceEdge(me))
  def threatsInFrames(f: Int) : Seq[UnitInfo] = threats.filter(_.framesToGetInRange(me) < f)
  def targetsInRange          : Seq[UnitInfo] = targets.filter(target => target.visible && me.pixelRangeAgainst(target) >= target.pixelDistanceEdge(me) && (me.unitClass.groundMinRangeRaw <= 0 || me.pixelDistanceEdge(target) > 32.0 * 3.0))
  lazy val arbiterCovering            : Cache[Boolean]    = new Cache(() => allies.exists(a => Protoss.Arbiter(a) && a.pixelDistanceEdge(me) < 160))
  lazy val allyTemplarCount           : Cache[Int]        = new Cache(() => allies.count(Protoss.HighTemplar))
  lazy val dpfReceiving               : Cache[Double]     = new Cache(() => threatsInRange.view.map(_.matchups.dpfDealingDiffused(me)).sum)
  lazy val framesToLive               : Double  = _framesToLive()
  lazy val framesOfSafety             : Double  = - With.latency.latencyFrames - With.reaction.agencyAverage - Maff.nanToZero(pixelsOfEntanglement / me.topSpeed)
  lazy val pixelsOfEntanglement       : Double  = _pixelsOfEntanglement()
  private val _framesToLive = new Cache(() => me.doomedInFrames)
  private val _pixelsOfEntanglement = new Cache(() => Maff.max(threats.map(me.pixelsOfEntanglement)).getOrElse(-With.mapPixelWidth.toDouble))

  protected def threatens(shooter: UnitInfo, victim: UnitInfo): Boolean = (
    shooter.canAttack(victim)
    || victim.friendly.exists(_.transport.exists(threatens(shooter, _)))
    || (victim.cloaked
      && shooter.unitClass.canAttack(victim)
      && shooter.unitClass.isDetector
      && shooter.framesToBeReadyForAttackOrder <= 8 + victim.framesToTravelPixels(shooter.pixelRangeAgainst(victim) - shooter.pixelDistanceEdge(victim))))

  def repairers: Seq[UnitInfo] = {
    allies.view.filter(a => a.unitClass == Terran.SCV && a.friendly.map(_.agent.toRepair.contains(me)).getOrElse(a.orderTarget.contains(me)))
  }

  private def dpfDealingDiffused(target: UnitInfo): Double = {
    me.dpfOnNextHitAgainst(target) / Math.max(1.0, targetsInRange.size)
  }
}
