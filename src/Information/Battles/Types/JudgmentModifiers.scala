package Information.Battles.Types

import Debugging.Visualizations.Colors
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Basic.Gather
import Planning.UnitMatchers.{MatchSiegeTank, MatchWorkers}
import ProxyBwapi.Races.Terran
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
    add("Aggression",   Colors.MidnightRed,   aggression(battle))
    add("Proximity",    Colors.NeonRed,       proximity(battle))
    add("Coherence",    Colors.MediumOrange,  coherence(battle))
    add("EnemyChoked",  Colors.MediumYellow,  enemyChoked(battle))
    add("Rout",         Colors.MediumGreen,   rout(battle))
    add("Maxed",        Colors.MediumTeal,    maxed(battle))
    add("Gatherers",    Colors.MediumBlue,    gatherers(battle))
    add("HornetNest",   Colors.NeonIndigo,    hornetNest(battle))
    add("Commitment",   Colors.NeonViolet,    commitment(battle))
    add("Patience",     Color.Black,          patience(battle))
    output
  }

  // Evaluate gains proportionate to aggression
  def aggression(local: BattleLocal): Option[JudgmentModifier] = {
    val aggro = With.blackboard.aggressionRatio()
    if (aggro == 1) None else Some(JudgmentModifier(gainedValueMultiplier = aggro))
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
    val distanceHome  = (if (keyBases.isEmpty) Seq(With.geography.home) else keyBases.map(_.heart.nearestWalkableTile)).map(centroid().groundPixels).min
    val distanceRatio = PurpleMath.clamp(distanceHome.toDouble / distanceMax, 0, 1)
    val multiplier    = 1.2 - 0.4 * distanceRatio
    Some(JudgmentModifier(gainedValueMultiplier = multiplier))
  }

  // Prefer fighting
  //  when we have a coherence advantage,
  //    because the benefits of this are underrepresented in simulation
  //      due to the absence of collisions
  def coherence(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    val us    = battleLocal.us.coherence()
    val enemy = battleLocal.enemy.coherence()
    val bonus = us - enemy
    Some(JudgmentModifier(speedMultiplier = 1 + 0.2 * bonus))
  }

  // Prefer fighting
  //  when the enemy is entering a choke
  //    because the benefits of this are underrepresented in simulation
  //      due to the absence of collisions,
  //    and because the situation is likely to get worse for us
  //      once they have gotten through the choke
  def enemyChoked(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    val choked = battleLocal.enemy.units.exists(unit =>
      ! unit.flying
      && unit.zone.edges.exists(edge =>
        edge.contains(unit.pixel)
        && edge.radiusPixels < battleLocal.enemy.widthIdeal() / 4d))

    None
    // TODO: This is proccing in bad situations like an enemy containing our natural from outside, but some of their units are in the choke AFTER that so not of salience to the battle
    // if (choked) Some(JudgmentModifier(speedMultiplier = 1.2)) else None
  }

  // Prefer fighting
  //   when the enemy is running away
  //     because we can get free damage in
  //     and because perhaps the fight is good for us
  //       due to reasons we haven't considered
  def rout(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    None
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
        g.pixelDistanceEdge(ally) <= Gather.defenseRadiusPixels
        && ally.matchups.threats.exists(_.pixelsToGetInRange(ally, g.pixel) <= Gather.defenseRadiusPixels))))
    val workersTotal = With.units.countOurs(MatchWorkers)
    val workersRatio = PurpleMath.nanToZero(workersImperiled.toDouble / workersTotal)
    if (workersRatio > 0) Some(JudgmentModifier(targetDelta = -workersRatio)) else None
  }

  // Avoid fighting
  //   into an enemy base that is likely to have reinforcements
  //   especially if those reinforcements are siege tanks
  //     because their existence is highly probable
  //     and if we attacked in error once, we will likely keep doing it
  //     and thus systematically bleed units
  def hornetNest(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    if (With.enemies.forall(e => ! e.isTerran || ! e.hasTech(Terran.SiegeMode))) return None
    val tanks           = battleLocal.enemy.units.count(u => u.is(MatchSiegeTank) && u.base.exists(_.owner.isEnemy) && ! u.visible)
    if (tanks == 0) return None
    def ourCombatUnits  = battleLocal.us.units.view.filter(_.canAttack)
    val valueUs         = ourCombatUnits.map(_.subjectiveValue).sum
    val valueUsGround   = ourCombatUnits.filterNot(_.flying).map(_.subjectiveValue).sum
    val ratioUsGround   = PurpleMath.nanToZero(valueUsGround / valueUs)
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
    val commitment = PurpleMath.mean(fighters.map(u => PurpleMath.clamp((32 + u.matchups.pixelsOfEntanglement) / 96d, 0, 1)))
    Some(JudgmentModifier(targetDelta = if (commitment > 0) -commitment * 0.2 else 0.2))
  }

  // Avoid engaging
  //   in a fight that's not obviously winnable
  //   until we have gained confidence over time that it will be winnable
  //     because high-frequency combat vacillation
  //     systematically causes more bad decisions and bleeding
  def patience(battleLocal: BattleLocal): Option[JudgmentModifier] = {
    None
  }
}
