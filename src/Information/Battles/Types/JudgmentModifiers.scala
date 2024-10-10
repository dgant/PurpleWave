package Information.Battles.Types

import Debugging.Visualizations.Colors
import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Basic.Gather
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?
import Utilities.Time.{Minutes, Seconds}
import Utilities.UnitFilters.{IsAny, IsTank, IsWorker}
import bwapi.Color

import scala.collection.mutable.ArrayBuffer

object JudgmentModifiers {

  def apply(battle: Battle): Seq[JudgmentModifier] = {
    val output = new ArrayBuffer[JudgmentModifier]
    def add(name: String, color: Color, modifier: Option[JudgmentModifier]): Unit = {
      modifier
        .filter(m =>
              ! m.targetDelta.isNaN
          &&  ! m.speedMultiplier.isNaN
          &&  (
                  Math.abs(m.targetDelta)         > 0.0001
              ||  Math.abs(m.speedMultiplier - 1) > 0.0001))
        .foreach(m => {
            m.name = name
            m.color = color
            output += m })
    }
    add("Proximity",    Colors.NeonRed,       proximity(battle))
    add("Gatherers",    Colors.MediumBlue,    gatherers(battle))
    add("HiddenTanks",  Colors.MediumIndigo,  hiddenTanks(battle))
    add("Choke",        Colors.MediumOrange,  choke(battle))
    //add("TankLock",     Colors.MediumRed,     tankLock(battle))
    add("Caught",       Colors.BrightGreen,   caught(battle))
    add("Hysteresis",   Colors.MediumViolet,  hysteresis(battle))
    output
  }

  private def modifyTarget  (value: Double): Option[JudgmentModifier] = Some(JudgmentModifier(targetDelta     = value))
  private def multiplySpeed (value: Double): Option[JudgmentModifier] = Some(JudgmentModifier(speedMultiplier = value))

  private def value       (units: Iterable[UnitInfo])                           : Double = units.view.map(_.subjectiveValue).sum
  private def value       (units: Iterable[UnitInfo], test: UnitInfo => Boolean): Double = value(units.view.filter(test))
  private def valueRatio  (units: Iterable[UnitInfo], test: UnitInfo => Boolean): Double = value(units, test) / Math.max(1, value(units))

  // Prefer fighting
  //  when close to home,
  //  especially against ranged units
  //  especially if pushed into our main/natural
  //    because we will run out of room to retreat
  //    and because workers or buildings will be endangered if we don't
  def proximity(battle: Battle): Option[JudgmentModifier] = {
    val enemyRange      = battle.enemy.meanAttackerRange
    val deltaMin        = -0.4 * Maff.clamp(With.frame.toDouble / Minutes(10)(), 0.5, 1.0)
    val deltaMax        = 0.05
    val proximity       = With.scouting.proximity(battle.enemy.centroidGround)
    val proximityMult   = (proximity * 2 - 1)
    val rangeMult       = Maff.clamp(enemyRange / (32 * 6.0), 0.0, 2.0)
    val targetDeltaRaw  = -0.3 * proximityMult * rangeMult
    val targetDelta     = Maff.clamp(targetDeltaRaw, deltaMin, deltaMax)
    modifyTarget(targetDelta)
  }

  // Prefer fighting
  //   when our gatherers are endangered
  //    because they are very fragile
  //    and if they die we will probably lose the game
  def gatherers(battle: Battle): Option[JudgmentModifier] = {
    val workersImperiled = battle.us.units.count(ally =>
      ally.unitClass.isWorker
      && ally.visibleToOpponents
      && ally.friendly.exists(_.agent.toGather.exists(g =>
        g.base.exists(_.owner.isUs) // Don't assist distance miners
        && g.pixelDistanceEdge(ally) <= Gather.defenseRadiusPixels
        && ally.matchups.threats.exists(t => t.pixelDistanceEdge(g) - t.pixelRangeAgainst(ally) <= Gather.defenseRadiusPixels))))
    val workersTotal = With.units.countOurs(IsWorker)
    val workersRatio = Maff.nanToZero(workersImperiled.toDouble / workersTotal)
    modifyTarget( - workersRatio / 2.0)
  }

  // Avoid fighting
  //   into an enemy base that is likely to have reinforcements
  //   especially if those reinforcements are siege tanks
  //     because their existence is highly probable
  //     and if we attacked in error once, we will likely keep doing it
  //     and thus systematically bleed units
  def hiddenTanks(battle: Battle): Option[JudgmentModifier] = {
    if (With.enemies.forall(e => ! e.isTerran || ! e.hasTech(Terran.SiegeMode))) return None
    val tanks           = battle.enemy.units.count(u => IsTank(u) && u.base.exists(_.owner.isEnemy) && ! u.visible)
    if (tanks == 0) return None
    val valueUs         = value(battle.us.attackers)
    val valueUsGround   = value(battle.us.attackers, ! _.flying)
    val ratioUsGround   = Maff.nanToZero(valueUsGround / valueUs)
    val score           = Maff.clamp(ratioUsGround * tanks * 0.1, 0.0, 0.4)
    multiplySpeed(1 - score)
  }

  // Avoid fighting across chokes/bridges
  def choke(battle: Battle): Option[JudgmentModifier] = {
    val pixelUs   = battle.us.attackCentroidGround
    val pixelFoe  = battle.enemy.vanguardGround()
    val edge      = battle.choke
    if (edge.isEmpty)                                                                                     return None
    if (pixelFoe.zone.bases.exists(With.geography.ourBasesAndSettlements.contains))                       return None
    if (pixelFoe.pixelDistance(edge.get.pixelCenter) + edge.get.radiusPixels < battle.us.maxRangeGround)  return None

    val badness   = edge.get.badness(battle.us, pixelUs.zone)
    if (badness <= 1)                                                                                     return None

    val speedMultiplier = battle.us.combatGroundFraction * Maff.nanToOne(1.0 / badness)
    val targetDelta     = battle.us.combatGroundFraction * Maff.clamp((badness - 1) * 0.0175, 0.0, 0.3)
    Some(JudgmentModifier(speedMultiplier = speedMultiplier, targetDelta = targetDelta))
  }

  // Prefer fighting tanks when we are already in range to attack them, or vice versa
  def tankLock(battle: Battle): Option[JudgmentModifier] = {
    if ( ! With.enemies.exists(_.isTerran))                   return None
    if ( ! battle.enemy.units.exists(Terran.SiegeTankSieged)) return None

    val inTankRangeRatio  = valueRatio(battle.us.attackers, _.matchups.inTankRange)
    val tankInRangeRatio  = valueRatio(battle.us.attackers, _.matchups.targetsInRange.exists(Terran.SiegeTankSieged))
    val inRankRangeScore  = inTankRangeRatio * 1/3d
    val tankInRangeScore  = tankInRangeRatio * 2/3d
    val score             = - 0.5 * (inRankRangeScore + tankInRangeScore)
    modifyTarget(score)
  }

  // Prefer fighting when caught by fast units
  private val catcher = IsAny(Zerg.Zergling, Terran.Vulture, Zerg.Mutalisk)
  def caught(battle: Battle): Option[JudgmentModifier] = {
    if ( ! battle.us.attackers.exists(_.matchups.engagedUpon)) return None
    val catcherRatio  = valueRatio(battle.enemy.attackers, catcher)
    val caughtRatio   = valueRatio(battle.us.attackers, u => ! u.flying && u.matchups.threatDeepest.exists(t => catcher(t) && t.pixelsToGetInRange(u) < 160 && t.topSpeed > 1.05 * u.topSpeed))
    val score         = -0.5 * Math.max(0, (1 - battle.us.vanguardGround().proximity) * Math.sqrt(Maff.nanToZero(catcherRatio * caughtRatio)))
    modifyTarget(score)
  }

  // Avoid disengaging
  //   from a fight we have already committed to
  //   especially if it is a large battle
  //     because leaving a battle is costly
  //     and if it's a large battle, our reinforcements won't make up
  //       for the units we lose in the retreat
  // Avoid engaging
  //   in a fight we have not yet committed to
  //   until conditions look advantageous
  //     because surprise is on the enemy's side
  //     and because patience will tend to let us gather more force to fight
  //
  // We apply a floor to any commitment to avoid systematically underweighing commitment and bleeding out
  private def commitmentFloor(value: Double): Double = if (value > 0) Math.max(0.25, value) else value
  def hysteresisOld(battle: Battle): Option[JudgmentModifier] = {
    val commitmentRaw             = Maff.weightedMean(battle.us.attackers.map(u => (Maff.clamp01(commitmentFloor((8 + u.matchups.pixelsEntangled) / 96d)), u.subjectiveValue)))
    val commitment                = commitmentFloor(commitmentRaw)
    lazy val groundRatio          = valueRatio(battle.us.attackers, ! _.flying)
    lazy val invisibleRatio       = valueRatio(battle.enemy.attackers, !_.visible)
    lazy val siegeRatio           = ?(With.enemies.exists(_.isTerran), valueRatio(battle.enemy.attackers, t => Terran.SiegeTankSieged(t) || (t.canSiege && With.framesSince(t.lastSeen) > 24)), 0)
    lazy val hesitanceVisibility  = 0.08 * invisibleRatio
    lazy val hesitanceTanks       = 0.12 * siegeRatio * groundRatio
    modifyTarget(?(commitment > 0, -commitment * 0.2, hesitanceVisibility + hesitanceTanks))
  }

  def hysteresis(battle: Battle): Option[JudgmentModifier] = {
    modifyTarget(Maff.weightedMean(battle.us.attackers.flatMap(_.friendly.map(hysteresis))))
  }
  private case class Motivation(var amount: Double = 0, var weight: Double = 0)
  def hysteresis(unit: FriendlyUnitInfo): (Double, Double) = {
    var amount: Double    =   0
    val fighting     =   unit.matchups.engagedUpon || unit.matchups.engagingOn
    val speed        =   Math.max(unit.topSpeed, 0.1)
    val entanglement =   Maff.clamp01(unit.matchups.pixelsEntangled / speed / Math.min(unit.matchups.framesToLive, Seconds(5)()))
    val recentEngage =   Math.max(0, 1 - unit.hysteresis.decisionFrames / Seconds(10)())
    val incomingShot =   Maff.fromBoolean(unit.damageQueue.view.exists(_.committed))
    val paidPrice    =   Math.max(0, 1 - incomingShot * Math.max(0, With.framesSince(unit.lastFrameTakingDamage) / Seconds(5)()))
    val stickiness   =   recentEngage * paidPrice
    val staticThreat =   Maff.fromBoolean(unit.matchups.threatDeepest.exists(t => t.unitClass.isBuilding || Terran.SiegeTankSieged(t) || (t.canSiege && With.framesSince(t.lastSeen) > 24)))
    val obscurity    =   unit.presumptiveTarget.map(target => Math.min(1, With.framesSince(target.lastSeen).toDouble / Seconds(60)())).getOrElse(0d)
    val pace         =   unit.squad.map(_.pace01).getOrElse(0)
    val patience     =   1 - Maff.clamp01(unit.hysteresis.bloodlustFrames.toDouble / Seconds(15)())
    val displacement =   Math.max(0, Maff.clamp01(Math.abs(unit.matchups.pixelsToTargetRange.flatMap(distance => unit.squad.map(_.meanAttackerTargetDistance - distance)).getOrElse(0d)) / speed / Seconds(15)()) - 0.333)
    val tankLock     =   Maff.or0(Maff.or0(.333, unit.matchups.inTankRange) + Maff.or0(0.666, unit.matchups.inRangeOfTank), With.enemies.exists(_.isTerran))

    if (fighting) {
      amount  -= .25  * entanglement
      amount  -= .15  * stickiness
      amount  -= .25  * tankLock
    } else {
      amount  += .10  * staticThreat
      amount  += .03  * obscurity
      //amount  +=  0   * pace
      amount  += .10  * patience
      amount  += .10  * displacement
    }
    if (With.frame < 0) {
      With.logger.debug(f"$fighting $speed $entanglement $stickiness $staticThreat $obscurity $pace $patience $displacement $tankLock")
    }
    (amount, unit.subjectiveValue * ?(fighting, 2, 1))
  }

  /*
  Hysteresis ideas:
  [x] STALL when last decision was flee
  [x] STALL vs static D or siegeable tanks
  [x] STALL vs invisible units
  [x] STALL when bloodthirst is low
  [~] STALL when pace is high
  [x] STALL when reinforcements are approaching
  [x] COMMIT when last decision was fight
  [x] COMMIT when deep in enemy range, especially if we're slow
  - COMMIT when enemy is in range, especially if units are juicy (reavers, tanks, workers, static D)
  - COMMIT when across a bridge
  - COMMIT when enemies are disabled
   */
}
