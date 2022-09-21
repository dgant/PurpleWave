package Micro.Matchups

import Information.Battles.BattleFilters
import Lifecycle.With
import Mathematics.Maff
import Performance.{Cache, KeyedCache}
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.UnitInfo
import Tactic.Squads.{GenericUnitGroup, UnitGroup}
import Utilities.?

case class MatchupAnalysis(me: UnitInfo) {
  // Default units allow identification of targets when destroying an empty base, because no Battle is happening
  // The necessity of this is a good argument for defining battles even if they would have trivial simulation results
  private def defaultUnits: Seq[UnitInfo] = if (me.canAttack) me.zone.units.view.filter(u => u.isEnemy && BattleFilters.local(u)) else Seq.empty.view

  def groupOf: UnitGroup = _groupOf()
  def groupVs: UnitGroup = _groupVs()
  private val _groupOf = new Cache(() => me.team.orElse(me.friendly.flatMap(_.squad)).getOrElse(GenericUnitGroup(Seq(me))))
  private val _groupVs = new KeyedCache(
    () => me.team.map(_.opponent)
      .orElse(me.friendly.flatMap(_.targetsAssigned).map(GenericUnitGroup))
      .getOrElse(GenericUnitGroup(defaultUnits)),
    () => (
      me.zone.units.size, // Modifying the zone units invalidates views based on the mutable underlying container (zone.units) and can cause IndexOutOfBoundsException
      me.friendly.flatMap(_.targetsAssigned))) // Modifying targetsAssigned invalidates any unit group constructed from it

  private def battleAll     : Option[Seq[UnitInfo]] = me.battle.map(_.teams.view.flatMap(_.units))
  private def battleUs      : Option[Seq[UnitInfo]] = me.team.map(_.units.view)
  private def battleEnemies : Option[Seq[UnitInfo]] = me.team.map(_.opponent.units.view)
  private def entrants      : Seq[UnitInfo]         = me.battle.flatMap(With.matchups.entrants.get).getOrElse(Seq.empty).view

  private def withEntrants(source: Seq[UnitInfo], filter: (UnitInfo) => Boolean = (unit) => true): Seq[UnitInfo] = source ++ entrants.filter(filter).filterNot(source.contains)

  def alliesInclSelf              : Seq[UnitInfo]     = battleUs.map(withEntrants(_, _.isFriendly)).getOrElse(groupOf.groupUnits.view)
  def allies                      : Seq[UnitInfo]     = alliesInclSelf.filterNot(_.id == me.id)
  def enemies                     : Seq[UnitInfo]     = battleEnemies.map(withEntrants(_, _.isEnemy)).getOrElse(groupVs.groupUnits.view)
  def others                      : Seq[UnitInfo]     = enemies ++ allies
  def allUnits                    : Seq[UnitInfo]     = others :+ me
  def threats                     : Seq[UnitInfo]     = enemies.filter(threatens(_, me)).filterNot(Protoss.Interceptor)
  def targets                     : Seq[UnitInfo]     = enemies.filter(threatens(me, _))
  def threatsInRange              : Seq[UnitInfo]     = threats.filter(threat => threat.pixelRangeAgainst(me) >= threat.pixelDistanceEdge(me))
  def threatsInRangeFrames(f: Int): Seq[UnitInfo]     = threats.filter(_.framesToGetInRange(me) < f)
  def threatDeepest               : Option[UnitInfo]  = _threatDeepest()
  def threatSoonest               : Option[UnitInfo]  = _threatSoonest()
  def threatNearest               : Option[UnitInfo]  = _threatNearest()
  def targetsInRange              : Seq[UnitInfo]     = targets.filter(target => target.visible && me.pixelRangeAgainst(target) >= target.pixelDistanceEdge(me) && (me.unitClass.groundMinRangeRaw <= 0 || me.pixelDistanceEdge(target) > 32.0 * 3.0))
  def targetNearest               : Option[UnitInfo]  = _targetNearest()
  def wantsToVolley               : Option[Boolean]   = _wantsToVolley() // None means no opinion
  lazy val dpfReceiving           : Cache[Double]     = new Cache(() => threatsInRange.view.map(t => t.dpfOnNextHitAgainst(me) / t.matchups.targetsInRange.size).sum)
  lazy val isCloakedAttacker      : Cache[Boolean]    = new Cache(() => me.cloaked && targets.nonEmpty)
  lazy val inTankRange            : Cache[Boolean]    = new Cache(() => threatsInRange.exists(Terran.SiegeTankSieged))
  lazy val framesToLive           : Double            = _framesToLive()
  lazy val framesOfSafety         : Double            = - With.latency.latencyFrames - With.reaction.agencyAverage - Maff.nanToZero(pixelsEntangled / me.topSpeed)
  lazy val pixelsEntangled        : Double            = _pixelsEntangled()
  private val _threatDeepest    = new Cache(() => Maff.minBy(threats)(t => t.pixelDistanceEdge(me) - t.pixelRangeAgainst(me)))
  private val _threatSoonest    = new Cache(() => Maff.minBy(threats)(_.framesToLaunchAttack(me)))
  private val _threatNearest    = new Cache(() => Maff.minBy(threats)(_.pixelDistanceEdge(me)))
  private val _targetNearest    = new Cache(() => Maff.minBy(targets)(_.pixelDistanceEdge(me)))
  private val _framesToLive     = new Cache(() => me.likelyDoomedInFrames)
  private val _pixelsEntangled  = new Cache(() => Maff.max(threats.map(me.pixelsOfEntanglement)).getOrElse(-With.mapPixelWidth.toDouble))
  private val _wantsToVolley    = new Cache(() => ?( ! me.canMove, None, // If we're stationary, we're unopinionated
    targetNearest.flatMap(target =>
      threatDeepest.flatMap(deepest =>
        threatNearest.flatMap(nearest =>
          // If we have time to volley before being engaged, we're unopinionated
          // The speed multiplier accounts for threat approaching us while we approach target
          threatSoonest
            .filter(soonest => (soonest.topSpeed + me.topSpeed) / me.topSpeed * me.framesToLaunchAttack(target) + me.unitClass.framesToPotshot < soonest.framesToLaunchAttack(me))
            .map(soonest =>
              // Volleying must not decrease our DPS relative to theirs.
              // Dragoon vs. anyone: Yes
              // Corsair vs. Mutalisk: No
              (me.cooldownMaxAgainst(target) > me.unitClass.framesToPotshot
                // Speed Hydra vs. Slow Zealot: Yes
                // Slow Hydra vs. Slow Zealot: No
                || (me.pixelRangeAgainst(target) > deepest.pixelRangeAgainst(me) && me.topSpeed > deepest.topSpeed)
              // Volleying must not let them open a gap against us
              // Dragoon vs. Cannon: Yes
              // Rangeless Dragoon vs. Ranged Dragoon: No
              && (me.pixelRangeAgainst(target) >= deepest.pixelRangeAgainst(me)
                || me.pixelRangeAgainst(target) >= nearest.pixelRangeAgainst(me)
                || me.topSpeed > deepest.topSpeed))))))))

  protected def threatens(shooter: UnitInfo, victim: UnitInfo): Boolean = (
    shooter.canAttack(victim)
    || victim.friendly.exists(_.transport.exists(threatens(shooter, _)))
    || (victim.cloaked
      && shooter.unitClass.canAttack(victim)
      && (shooter.unitClass.isDetector || groupVs.mobileDetectors.nonEmpty)
      && shooter.framesToBeReadyForAttackOrder <= Maff.nanToInfinity((64 + shooter.pixelRangeAgainst(victim) - shooter.pixelDistanceEdge(victim)) / (shooter.topSpeed + victim.topSpeed))))

  def repairers: Seq[UnitInfo] = {
    allies.view.filter(a => Terran.SCV(a) && a.friendly.map(_.agent.toRepair.contains(me)).getOrElse(a.orderTarget.contains(me)))
  }
}
