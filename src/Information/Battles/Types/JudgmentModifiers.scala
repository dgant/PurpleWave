package Information.Battles.Types

import Debugging.Visualizations.Colors
import Information.Geography.Pathfinding.PathfindProfile
import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Basic.Gather
import Planning.UnitMatchers.{MatchTank, MatchWorker}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import bwapi.Color

import scala.collection.mutable.ArrayBuffer

object JudgmentModifiers {

  def apply(battle: BattleLocal): Seq[JudgmentModifier] = {
    val output = new ArrayBuffer[JudgmentModifier]
    def add(name: String, color: Color, modifier: Option[JudgmentModifier]) {
      modifier.foreach(m => {
        m.name = name
        m.color = color
        output += m })
    }
    add("Aggression", Colors.MidnightRed,   aggression(battle))
    add("Choke",      Colors.BrightGreen,   choke(battle))
    add("Proximity",  Colors.NeonRed,       proximity(battle))
    add("Maxed",      Colors.MediumTeal,    maxed(battle))
    add("Gatherers",  Colors.MediumBlue,    gatherers(battle))
    add("HornetNest", Colors.NeonIndigo,    hornetNest(battle))
    add("Commitment", Colors.NeonViolet,    commitment(battle))
    add("Towers",     Colors.MediumGreen,   towers(battle))
    add("Anchors",    Colors.MediumOrange,   anchored(battle))
    output
  }

  // Evaluate gains proportionate to aggression
  def aggression(local: BattleLocal): Option[JudgmentModifier] = {
    val aggro = With.blackboard.aggressionRatio()
    if (aggro == 1) None else Some(JudgmentModifier(gainedValueMultiplier = aggro))
  }

  // Avoid fighting through a choke
  // Prefer fighting when the enemy is passing through one
  def choke(local: BattleLocal): Option[JudgmentModifier] = {
    if (local.us.attackers.forall(_.flying)) return None
    if (local.enemy.attackers.forall(_.flying)) return None
    val start   = local.us.vanguardGround().tile
    val profile = new PathfindProfile(start)
    profile.end = Some(local.enemy.vanguardGround().tile)
    profile.employGroundDist = true
    profile.lengthMaximum = Some(1 + local.enemy.attackers.view.map(_.effectiveRangePixels).max.toInt / 32)
    val path = profile.find
    if ( ! path.pathExists) return None
    val chokeTile = path.tiles.get.view.map(t => (t, t.zone.edges.find(_.contains(t)))).find(_._2.isDefined)
    if (chokeTile.isDefined) {
      val tile                = chokeTile.get._1
      val choke               = chokeTile.get._2.get
      val ourSide             = choke.endPixels.minBy(_.groundPixels(start))
      val enemySide           = choke.endPixels.maxBy(_.groundPixels(start))
      val distance            = tile.pixelDistanceGround(start)
      val enemiesThrough      = local.enemy.units.count(u => u.flying || u.pixelDistanceTravelling(start) < distance)
      val enemiesIn           = local.enemy.units.count(u => ! u.flying && choke.contains(u.pixel) && u.pixelDistanceSquared(ourSide) < u.pixelDistanceSquared(enemySide))
      val denominator         = Math.max(1, local.enemy.units.size)
      val enemiesThroughRatio = enemiesThrough / denominator
      val enemiesInRatio      = enemiesIn / denominator
      if (enemiesThroughRatio == 0 && enemiesInRatio == 0) {
        val speedRatio        = Maff.clamp(4 * choke.radiusPixels / Math.max(1, local.us.widthPixels), 0.25, 1.0)
        return Some(JudgmentModifier(speedMultiplier = speedRatio))
      } else if (enemiesInRatio > 0 && enemiesThroughRatio < 0.25) {
        return Some(JudgmentModifier(targetDelta = -0.1))
      }
    }
    None
  }

  // Prefer fighting
  //  when close to home,
  //  especially if pushed into our main/natural
  //    because we will run out of room to retreat
  //    and because workers or buildings will be endangered if we don't
  def proximity(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    val centroid      = battleLocal.enemy.centroidGround
    val keyBases      = With.geography.ourBasesAndSettlements.filter(b => b.isOurMain || b.isNaturalOf.exists(_.isOurMain))
    val distanceMax   = With.mapPixelWidth
    val distanceHome  = (if (keyBases.isEmpty) Seq(With.geography.home) else keyBases.map(_.heart.nearestWalkableTile)).map(centroid.groundPixels).min
    val distanceRatio = Maff.clamp(distanceHome.toDouble / distanceMax, 0, 1)
    val multiplier    = 1.2 - 0.4 * distanceRatio
    Some(JudgmentModifier(gainedValueMultiplier = multiplier))
  }

  // Prefer fighting
  //   when we are maxed out
  //   especially with a bank
  //     because from here the enemy will only get stronger relative to us
  def maxed(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    val maxedness = (With.self.supplyUsed + With.self.minerals / 25d) / 400d
    val targetDelta = .9 - maxedness
    if (targetDelta < 0) Some(JudgmentModifier(targetDelta = targetDelta)) else None
  }

  // Prefer fighting
  //   when our gatherers are endangered
  //    because they are very fragile
  //    and if they die we will probably lose the game
  def gatherers(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    val workersImperiled = battleLocal.us.units.count(ally =>
      ally.unitClass.isWorker
      && ally.visibleToOpponents
      && ally.friendly.exists(_.agent.toGather.exists(g =>
        g.base.exists(_.owner.isUs) // Don't assist distance miners
        && g.pixelDistanceEdge(ally) <= Gather.defenseRadiusPixels
        && ally.matchups.threats.exists(t => t.pixelDistanceEdge(g) - t.pixelRangeAgainst(ally) <= Gather.defenseRadiusPixels))))
    val workersTotal = With.units.countOurs(MatchWorker)
    val workersRatio = Maff.nanToZero(workersImperiled.toDouble / workersTotal)
    if (workersRatio > 0) Some(JudgmentModifier(targetDelta = -workersRatio / 2.0)) else None
  }

  // Avoid fighting
  //   into an enemy base that is likely to have reinforcements
  //   especially if those reinforcements are siege tanks
  //     because their existence is highly probable
  //     and if we attacked in error once, we will likely keep doing it
  //     and thus systematically bleed units
  def hornetNest(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    if (With.enemies.forall(e => ! e.isTerran || ! e.hasTech(Terran.SiegeMode))) return None
    val tanks           = battleLocal.enemy.units.count(u => u.is(MatchTank) && u.base.exists(_.owner.isEnemy) && ! u.visible)
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
  def commitment(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    def fighters = battleLocal.us.units.view.filter(_.unitClass.attacksOrCastsOrDetectsOrTransports)
    val commitment = Maff.mean(fighters.map(u => Maff.clamp((32 + u.matchups.pixelsOfEntanglement) / 96d, 0, 1)))
    Some(JudgmentModifier(targetDelta = if (commitment > 0) -commitment * 0.15 else 0.15))
  }

  // Avoid disengaging
  //   from a fight we have already committed to
  //   if we have unshuttled Reavers engaged
  //     because they are crawly slugs
  //     and love dying
  def anchored(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    lazy val fragileSlugs = battleLocal.us.units.exists(u =>
      u.isAny(Terran.SiegeTankSieged, Protoss.Reaver, Zerg.Lurker)
      && ! u.friendly.exists(_.agent.ride.isDefined)
      && u.matchups.pixelsOfEntanglement > -32)
    if (With.self.isProtoss && battleLocal.us.engagedUpon && fragileSlugs) {
      Some(JudgmentModifier(targetDelta = -0.25))
    } else None
  }

  // Avoid fighting
  //   when the enemy has static defense at home
  //   and we are equal on base count
  //     because this is not a great use of resources
  def towers(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    val satisfied = With.self.bases.size >= (if (With.self.isZerg) 1 else 0) + With.enemies.view.map(e => e.bases.size + (if (e.isZerg) 1 else 0)).max
    lazy val haveAir = battleLocal.us.units.exists(_.flying)
    lazy val haveGround = battleLocal.us.units.exists( ! _.flying)
    lazy val staticDefenseCount = battleLocal.enemy.units.count(u => u.complete && u.unitClass.isBuilding && ((haveAir && u.unitClass.attacksAir) || (haveGround && u.unitClass.attacksGround)))
    if (satisfied && staticDefenseCount > 0) {
      Some(JudgmentModifier(gainedValueMultiplier = 1.0 - 0.1 * Math.min(5, staticDefenseCount)))
    } else None
  }
}
