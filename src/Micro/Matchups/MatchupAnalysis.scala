package Micro.Matchups

import Information.Battles.BattleClassificationFilters
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Scouting.BlockConstruction
import Micro.Heuristics.MicroValue
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

case class MatchupAnalysis(me: UnitInfo) {
  // Default units allow identification of targets when destroying an empty base, because no Battle is happening
  // The necessity of this is a good argument for defining battles even if they would have trivial simulation results
  private def defaultUnits  : Seq[UnitInfo]         = if (me.canAttack) me.zone.units.view.filter(u => u.isEnemy && BattleClassificationFilters.isEligibleLocal(u)) else Seq.empty.view
  private def battleUnits   : Option[Seq[UnitInfo]] = me.battle.map(_.teams.view.flatMap(_.units))
  private def battleEnemies : Option[Seq[UnitInfo]] = me.battle.map(_.teamOf(me).opponent.units.view)
  private def battleUs      : Option[Seq[UnitInfo]] = me.battle.map(_.teamOf(me).units.view)
  private def entrants      : Seq[UnitInfo]         = me.battle.flatMap(With.matchups.entrants.get).getOrElse(Seq.empty).view

  private def withEntrants(source: Seq[UnitInfo]): Seq[UnitInfo] = source ++ entrants.filterNot(source.contains)

  def allUnits              : Seq[UnitInfo] = battleUnits.map(withEntrants).getOrElse(defaultUnits)
  def enemies               : Seq[UnitInfo] = battleEnemies.map(withEntrants).getOrElse(defaultUnits.filter(_.isEnemyOf(me)))
  def alliesInclSelf        : Seq[UnitInfo] = battleUs.map(withEntrants).getOrElse(defaultUnits.filterNot(_.isEnemyOf(me)))
  def alliesInclSelfCloaked : Seq[UnitInfo] = alliesInclSelf.filter(_.cloakedOrBurrowed)
  def allies                : Seq[UnitInfo] = alliesInclSelf.filterNot(_.id == me.id)
  def others                : Seq[UnitInfo] = enemies ++ allies
  def enemyDetectors        : Seq[UnitInfo] = enemies.filter(e => e.aliveAndComplete && e.unitClass.isDetector)
  def threats               : Seq[UnitInfo] = enemies.filter(threatens(_, me))
  def targets               : Seq[UnitInfo] = enemies.filter(threatens(me, _))
  def threatsInRange        : Seq[UnitInfo] = threats.filter(threat => threat.pixelRangeAgainst(me) >= threat.pixelDistanceEdge(me))
  def targetsInRange        : Seq[UnitInfo] = targets.filter(target => target.visible && me.pixelRangeAgainst(target) >= target.pixelDistanceEdge(me) && (me.unitClass.groundMinRangeRaw <= 0 || me.pixelDistanceEdge(target) > 32.0 * 3.0))
  lazy val nearestArbiter                : Option[UnitInfo]      = ByOption.minBy(allies.view.filter(_.is(Protoss.Arbiter)))(_.pixelDistanceSquared(me))
  lazy val allyTemplarCount              : Int                   = allies.count(_.is(Protoss.HighTemplar))
  lazy val busyForCatching               : Boolean               = me.gathering || me.constructing || me.repairing || ! me.canMove || BlockConstruction.buildOrders.contains(me.order)
  lazy val catchers                      : Vector[UnitInfo]      = threats.filter(canCatchMe).toVector
  lazy val splashFactorMax               : Double                = splashFactorForUnits(targets)
  lazy val splashFactorInRange           : Double                = splashFactorForUnits(targetsInRange)
  lazy val valuePerDamage                : Double                = MicroValue.valuePerDamageCurrentHp(me)
  lazy val vpfDealingInRange             : Double                = splashFactorInRange * ByOption.max(targetsInRange.map(MicroValue.valuePerFrameCurrentHp(me, _))).getOrElse(0.0)
  lazy val dpfReceiving                  : Double                = threatsInRange.view.map(_.matchups.dpfDealingDiffused(me)).sum
  lazy val vpfReceiving                  : Double                = valuePerDamage * dpfReceiving
  lazy val framesToLive                  : Double                = PurpleMath.nanToInfinity(me.totalHealth / dpfReceiving)
  lazy val pixelsOfEntanglement          : Double                = ByOption.max(threats.map(me.pixelsOfEntanglement)).getOrElse(- With.mapPixelWidth)
  lazy val framesOfSafety                : Double                = - With.latency.latencyFrames - With.reaction.agencyAverage - PurpleMath.nanToZero(pixelsOfEntanglement / me.topSpeed)
  lazy val pixelsOutOfNonWorkerRange     : Double                = ByOption.min(threats.view.filterNot(_.unitClass.isWorker).map(t => t.pixelDistanceEdge(me) - t.pixelRangeAgainst(me))).getOrElse(With.mapPixelWidth)

  protected def threatens(shooter: UnitInfo, victim: UnitInfo): Boolean = (
    shooter.canAttack(victim)
    || (shooter.unitClass.canAttack(victim)
      && victim.detected
      && shooter.framesToBeReadyForAttackOrder < victim.framesToTravelPixels(shooter.pixelRangeAgainst(victim) - shooter.pixelDistanceEdge(victim))))

  def repairers: Seq[UnitInfo] = {
    allies.view.filter(a => a.unitClass == Terran.SCV && a.friendly.map(_.agent.toRepair.contains(me)).getOrElse(a.orderTarget.contains(me)))
  }

  protected def splashFactorForUnits(targetsConsidered: Iterable[UnitInfo]): Double = {
    PurpleMath.clamp(me.unitClass.splashFactor, 1.0, targetsConsidered.size)
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
        // The Math.max prevents Zealots from thinking they can catch SCVs
        || (catcher.pixelRangeAgainst(me) > Math.max(32 * 2, me.effectiveRangePixels) && catcher.friendly.exists(_.squadenemies.contains(me)))
        || busyForCatching
        || catcher.is(Zerg.Scourge)
        || catcher.framesToGetInRange(me) < 8
        || (me.unitClass.isWorker && me.base.exists(_.harvestingArea.contains(me.tileIncludingCenter)))
        || (catcher.is(Zerg.Zergling) && With.self.hasUpgrade(Zerg.ZerglingSpeed) && ! me.player.hasUpgrade(Terran.VultureSpeed))
        || (catcher.is(Protoss.Zealot) && me.isDragoon() && ! me.player.hasUpgrade(Protoss.DragoonRange))
        || (catcher.is(Protoss.DarkTemplar) && me.isDragoon()))
    output
  }
}
