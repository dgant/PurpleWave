package Information.Battles.Types

import Debugging.Visualizations.Colors
import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Basic.Gather
import ProxyBwapi.Races.Terran
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsTank, IsWorker}
import bwapi.Color

import scala.collection.mutable.ArrayBuffer

object JudgmentModifiers {

  def apply(battle: Battle): Seq[JudgmentModifier] = {
    val output = new ArrayBuffer[JudgmentModifier]
    def add(name: String, color: Color, modifier: Option[JudgmentModifier]): Unit = {
      modifier.foreach(m => {
        m.name = name
        m.color = color
        output += m })
    }
    add("Proximity",    Colors.NeonRed,       proximity(battle))
    add("Gatherers",    Colors.MediumBlue,    gatherers(battle))
    add("HiddenTanks",  Colors.MediumIndigo,  hiddenTanks(battle))
    add("Commitment",   Colors.MediumViolet,  commitment(battle))
    add("Choke",        Colors.MediumOrange,  choke(battle))
    output
  }
  // Prefer fighting
  //  when close to home,
  //  especially against ranged units
  //  especially if pushed into our main/natural
  //    because we will run out of room to retreat
  //    and because workers or buildings will be endangered if we don't
  def proximity(battleLocal: Battle): Option[JudgmentModifier] = {
    val enemyRange      = battleLocal.enemy.meanAttackerRange
    val deltaMin        = -0.4 * Maff.clamp(With.frame.toDouble / Minutes(10)(), 0.5, 1.0)
    val deltaMax        = 0.05
    val proximity       = With.scouting.proximity(battleLocal.enemy.centroidGround)
    val proximityMult   = (proximity * 2 - 1)
    val rangeMult       = Maff.clamp(enemyRange / (32 * 6.0), 0.0, 2.0)
    val targetDeltaRaw  = -0.3 * proximityMult * rangeMult
    val targetDelta     = Maff.clamp(targetDeltaRaw, deltaMin, deltaMax)
    Some(JudgmentModifier(targetDelta = targetDelta))
  }

  // Prefer fighting
  //   when our gatherers are endangered
  //    because they are very fragile
  //    and if they die we will probably lose the game
  def gatherers(battleLocal: Battle): Option[JudgmentModifier] = {
    val workersImperiled = battleLocal.us.units.count(ally =>
      ally.unitClass.isWorker
      && ally.visibleToOpponents
      && ally.friendly.exists(_.agent.toGather.exists(g =>
        g.base.exists(_.owner.isUs) // Don't assist distance miners
        && g.pixelDistanceEdge(ally) <= Gather.defenseRadiusPixels
        && ally.matchups.threats.exists(t => t.pixelDistanceEdge(g) - t.pixelRangeAgainst(ally) <= Gather.defenseRadiusPixels))))
    val workersTotal = With.units.countOurs(IsWorker)
    val workersRatio = Maff.nanToZero(workersImperiled.toDouble / workersTotal)
    if (workersRatio > 0) Some(JudgmentModifier(targetDelta = -workersRatio / 2.0)) else None
  }

  // Avoid fighting
  //   into an enemy base that is likely to have reinforcements
  //   especially if those reinforcements are siege tanks
  //     because their existence is highly probable
  //     and if we attacked in error once, we will likely keep doing it
  //     and thus systematically bleed units
  def hiddenTanks(battleLocal: Battle): Option[JudgmentModifier] = {
    if (With.enemies.forall(e => ! e.isTerran || ! e.hasTech(Terran.SiegeMode))) return None
    val tanks           = battleLocal.enemy.units.count(u => IsTank(u) && u.base.exists(_.owner.isEnemy) && ! u.visible)
    if (tanks == 0) return None
    def ourCombatUnits  = battleLocal.us.units.view.filter(_.canAttack)
    val valueUs         = ourCombatUnits.map(_.subjectiveValue).sum
    val valueUsGround   = ourCombatUnits.filterNot(_.flying).map(_.subjectiveValue).sum
    val ratioUsGround   = Maff.nanToZero(valueUsGround / valueUs)
    val enemyBonus      = Math.min(ratioUsGround * tanks * 0.1, 0.4)
    if (enemyBonus > 0) Some(JudgmentModifier(speedMultiplier = 1 - enemyBonus)) else None
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
  private def commitmentFloor(value: Double): Double = if (value > 0) Math.max(0.5, value) else value
  def commitment(battleLocal: Battle): Option[JudgmentModifier] = {
    def fighters      = battleLocal.us.units.view.filter(_.unitClass.attacksOrCastsOrDetectsOrTransports)
    val commitmentRaw = Maff.mean(fighters.map(u => Maff.clamp(commitmentFloor((8 + u.matchups.pixelsOfEntanglement) / 96d), 0, 1)))
    val commitment    = commitmentFloor(commitmentRaw)
    Some(JudgmentModifier(targetDelta = if (commitment > 0) -commitment * 0.15 else 0.15))
  }

  // Avoid fighting across chokes/bridges
  def choke(battleLocal: Battle): Option[JudgmentModifier] = {
    val pUs       = battleLocal.us.attackCentroidGround
    val pFoe      = battleLocal.enemy.vanguardGround()
    val edge      = Maff.minBy(pUs.zone.edges.filter(_.otherSideof(pUs.zone) == pFoe.zone))(e => e.pixelCenter.pixelDistanceSquared(pUs) + e.pixelCenter.pixelDistanceSquared(pFoe))
    if (pUs.zone == pFoe.zone) return None
    if (battleLocal.us.units.exists(u => u.zone == pUs.zone && u.matchups.threatsInRange.exists(_.zone == pFoe.zone))) return None
    if (edge.isEmpty) return None
    val ranks     = battleLocal.us.widthPixels / Math.max(1.0, edge.get.diameterPixels)
    val speedMod  = battleLocal.us.combatGroundFraction * Maff.nanToOne(1.0 / ranks)
    val deltaMod  = battleLocal.us.combatGroundFraction * Maff.clamp((ranks - 1)* 0.0125, 0.0, 0.3)
    Some(JudgmentModifier(speedMultiplier = speedMod, targetDelta = deltaMod))
  }
}
