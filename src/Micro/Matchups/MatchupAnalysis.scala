package Micro.Matchups

import Information.Battles.BattleClassificationFilters
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Scouting.BlockConstruction
import Micro.Heuristics.MicroValue
import Performance.Cache
import Planning.UnitMatchers.MatchTank
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

case class MatchupAnalysis(me: UnitInfo) {
  // Default units allow identification of targets when destroying an empty base, because no Battle is happening
  // The necessity of this is a good argument for defining battles even if they would have trivial simulation results
  private def defaultUnits  : Seq[UnitInfo]         = if (me.canAttack) me.zone.units.view.filter(u => u.isEnemy && BattleClassificationFilters.isEligibleLocal(u)) else Seq.empty.view
  private def battleUnits   : Option[Seq[UnitInfo]] = me.battle.map(_.teams.view.flatMap(_.units))
  private def battleEnemies : Option[Seq[UnitInfo]] = me.team.map(_.opponent.units.view)
  private def battleUs      : Option[Seq[UnitInfo]] = me.team.map(_.units.view)
  private def entrants      : Seq[UnitInfo]         = me.battle.flatMap(With.matchups.entrants.get).getOrElse(Seq.empty).view

  private def withEntrants(source: Seq[UnitInfo], filter: (UnitInfo) => Boolean = (unit) => true): Seq[UnitInfo] = source ++ entrants.filter(filter).filterNot(source.contains)

  def allUnits                : Seq[UnitInfo] = battleUnits.map(withEntrants(_)).getOrElse(defaultUnits)
  def enemies                 : Seq[UnitInfo] = battleEnemies.map(withEntrants(_, _.isEnemy)).getOrElse(defaultUnits.filter(_.isEnemyOf(me)))
  def alliesInclSelf          : Seq[UnitInfo] = battleUs.map(withEntrants(_, _.isFriendly)).getOrElse(defaultUnits.filterNot(_.isEnemyOf(me)))
  def alliesInclSelfCloaked   : Seq[UnitInfo] = alliesInclSelf.filter(_.cloakedOrBurrowed)
  def allies                  : Seq[UnitInfo] = alliesInclSelf.filterNot(_.id == me.id)
  def others                  : Seq[UnitInfo] = enemies ++ allies
  def enemyDetectors          : Seq[UnitInfo] = enemies.filter(e => e.aliveAndComplete && e.unitClass.isDetector)
  def threats                 : Seq[UnitInfo] = enemies.filter(threatens(_, me))
  def targets                 : Seq[UnitInfo] = enemies.filter(threatens(me, _))
  def threatsInRange          : Seq[UnitInfo] = threats.filter(threat => threat.pixelRangeAgainst(me) >= threat.pixelDistanceEdge(me))
  def threatsInFrames(f: Int) : Seq[UnitInfo] = threats.filter(_.framesToGetInRange(me) < f)
  def targetsInRange          : Seq[UnitInfo] = targets.filter(target => target.visible && me.pixelRangeAgainst(target) >= target.pixelDistanceEdge(me) && (me.unitClass.groundMinRangeRaw <= 0 || me.pixelDistanceEdge(target) > 32.0 * 3.0))
  lazy val anchor                     : Option[UnitInfo]  = ByOption.minBy(anchors.filter(_.unitClass.subjectiveValue == anchors.view.map(_.unitClass.subjectiveValue).max))(a => a.pixelDistanceEdge(me) + a.presumptiveTarget.map(a.pixelsToGetInRange).getOrElse(a.pixelDistanceTravelling(a.presumptiveDestination)))
  lazy val anchors                    : Vector[UnitInfo]  = me.friendly.map(_.alliesSquad).getOrElse(allies).filter(doesAnchor(_, me)).toVector
  lazy val arbiterCovering            : Cache[Boolean]    = new Cache(() => allies.exists(a => Protoss.Arbiter(a) && a.pixelDistanceEdge(me) < 160))
  lazy val allyTemplarCount           : Int               = allies.count(Protoss.HighTemplar)
  lazy val busyForCatching            : Boolean           = me.unitClass.isWorker && (me.gathering || me.constructing || me.repairing || BlockConstruction.constructionOrders.contains(me.order))
  lazy val catchers                   : Vector[UnitInfo]  = threats.filter(canCatchMe).toVector
  lazy val splashFactorMax            : Double            = splashFactorForUnits(targets)
  lazy val splashFactorInRange        : Double            = splashFactorForUnits(targetsInRange)
  lazy val valuePerDamage             : Double            = MicroValue.valuePerDamageCurrentHp(me)
  lazy val vpfDealingInRange          : Double            = splashFactorInRange * ByOption.max(targetsInRange.map(MicroValue.valuePerFrameCurrentHp(me, _))).getOrElse(0.0)
  lazy val dpfReceiving               : Double            = threatsInRange.view.map(_.matchups.dpfDealingDiffused(me)).sum
  lazy val vpfReceiving               : Double            = valuePerDamage * dpfReceiving
  lazy val framesToLive               : Double            = PurpleMath.nanToInfinity(me.totalHealth / dpfReceiving)
  lazy val framesOfSafety             : Double            = - With.latency.latencyFrames - With.reaction.agencyAverage - PurpleMath.nanToZero(pixelsOfEntanglement / me.topSpeed)
  lazy val pixelsOfEntanglement       : Double            = ByOption.max(threats.map(me.pixelsOfEntanglement)).getOrElse(- With.mapPixelWidth)
  lazy val pixelsToReachAnyTarget     : Double            = ByOption.max(targets.map(me.pixelsToGetInRange)).getOrElse(With.mapPixelWidth)

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

  protected def splashFactorForUnits(targetsConsidered: Iterable[UnitInfo]): Double = {
    if (With.reaction.sluggishness > 0) me.unitClass.splashFactor else PurpleMath.clamp(me.unitClass.splashFactor, 1.0, targetsConsidered.size)
  }

  def dpfDealingDiffused(target: UnitInfo): Double = {
    splashFactorInRange * me.dpfOnNextHitAgainst(target) / Math.max(1.0, targetsInRange.size)
  }

  def canCatchMe(catcher: UnitInfo): Boolean = {
    if (catcher.unitClass.isWorker) return false
    val catcherSpeed = Math.max(catcher.topSpeed, catcher.friendly.flatMap(_.transport.map(_.topSpeed)).getOrElse(0.0))
    val catcherFlying = catcher.flying || catcher.friendly.exists(_.agent.ride.exists(_.flying)) && ! me.flying
    val output = (
      catcherSpeed * (if (catcherFlying) 2 else 1) >= me.topSpeed
        || catcher.pixelRangeAgainst(me) > 32 * 3
        || busyForCatching
        || catcher.is(Zerg.Scourge)
        || (catcher.framesToGetInRange(me) < 8 && me.speedApproaching(catcher) > 0)
        || (catcher.isAny(Terran.Marine, Protoss.Zealot) && me.is(Protoss.Dragoon) && ! me.player.hasUpgrade(Protoss.DragoonRange))
        || (catcher.is(Protoss.DarkTemplar) && me.is(Protoss.Dragoon)))
    output
  }

  private def doesAnchor(anchor: UnitInfo, support: UnitInfo): Boolean = {
    if (anchor.unitClass == support.unitClass) return false // Safety valve
    var output = false
    output ||= anchor.unitClass.isBuilding && anchor.unitClass.canAttack  && support.isAny(Terran.Marine, Terran.Goliath)
    output ||= anchor.isAny(MatchTank, Terran.Battlecruiser)         && ! support.isAny(MatchTank, Terran.Battlecruiser)
    output ||= anchor.isAny(Terran.Medic)                                 && support.isAny(Terran.Marine, Terran.Firebat)
    output ||= anchor.isAny(Terran.Marine)                                && support.isAny(Terran.SCV)
    output ||= anchor.isAny(Protoss.Carrier)                              && ! support.isAny(Protoss.Carrier)
    output ||= anchor.isAny(Protoss.Arbiter, Protoss.Reaver)              && support.isAny(Protoss.Zealot, Protoss.Dragoon, Protoss.Archon, Protoss.HighTemplar, Protoss.Corsair)
    //output ||= anchor.isAny(Protoss.HighTemplar) && anchor.energy > 65 && anchor.player.hasTech(Protoss.PsionicStorm)    && support.isAny(Protoss.Zealot, Protoss.Dragoon, Protoss.Archon)
    output ||= anchor.isAny(Protoss.Dragoon, Protoss.Archon)              && support.isAny(Protoss.Zealot)
    output ||= anchor.isAny(Zerg.Lurker, Zerg.Ultralisk)                  && support.isAny(Zerg.Zergling, Zerg.Hydralisk)
    output ||= anchor.isAny(Zerg.Guardian)                                && ! support.isAny(Zerg.Guardian)
    output ||= anchor.isAny(Zerg.Mutalisk, Zerg.Devourer)                 && ! support.isAny(Zerg.Scourge)
    output
  }
}
