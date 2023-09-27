package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Performance.Cache
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.?
import Utilities.Time.{GameTime, Minutes, Seconds}
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters._
import Utilities.UnitPreferences.PreferClose

class SquadScoutExpansions extends Squad {

  def frameToScout: GameTime = {
    if (With.self.isProtoss) {
      if      (With.enemy.isTerran)   GameTime(9, 0)
      else if (With.enemy.isProtoss)  GameTime(9, 0)
      else                            GameTime(7, 0)
    } else if (With.self.isTerran) {
      if      (With.enemy.isTerran)   GameTime(8, 0)
      else if (With.enemy.isProtoss)  GameTime(7, 0)
      else                            GameTime(7, 0)
    } else {
      if      (With.enemy.isTerran)   GameTime(9, 0)
      else if (With.enemy.isProtoss)  GameTime(9, 0)
      else                            GameTime(12, 0)
    }
  }

  private val scoutClasses = Seq(
    Terran.Marine,
    Terran.Firebat,
    Terran.Vulture,
    Terran.Goliath,
    Terran.Wraith,
    Protoss.Zealot,
    Protoss.Dragoon,
    Protoss.DarkTemplar,
    Protoss.Observer,
    Protoss.Corsair,
    Zerg.Zergling,
    Zerg.Hydralisk,
    Zerg.Overlord,
    Zerg.Scourge,
    Zerg.Queen)
  val matchAll    : UnitFilter = IsAny((scoutClasses :+ Is(scoutZergThird && IsWorker(_))): _*)
  val matchFlying : UnitFilter = IsAny(scoutClasses.filter(_.canFly): _*)

  val scoutableBases = new Cache(() =>
    With.geography.neutralBases
      .view
      .filterNot(With.scouting.enemyMain.contains)
      .filterNot(With.scouting.enemyNatural.contains)
      .filterNot(b =>
        With.scouting.enemyMain.exists(m =>
          m.metro == b.metro
          && With.scouting.enemyNatural.exists(n =>
            b.heart.groundTiles(m.heart) < n.heart.groundTiles(m.heart))))
      .toVector
      .sortBy(base => - With.scouting.baseIntrigue.getOrElse(base, 0.0))
      .sortBy(b => lock.units.exists(_.flying) || b.island))

  val scoutZergCutoff: Int = Minutes(4)()
  def scoutZergThird: Boolean = (
    With.frame < scoutZergCutoff
    && ! With.self.isZerg
    && With.enemies.exists(_.isZerg)
    && With.tactics.scoutWithWorkers.scoutingAbandoned)

  def launch(): Unit = {
    if ( ! With.blackboard.scoutExpansions()) return
    if ( ! scoutZergThird) {
      if (With.frame < frameToScout()) return
      if ( ! With.blackboard.wantToAttack() && ! With.blackboard.wantToHarass()) return
    }
    if (scoutableBases().isEmpty) return

    val toScoutBases = Maff.orElse(
      scoutableBases().filter(b => With.framesSince(b.lastFrameScoutedByUs) > Seconds(90)()),
      scoutableBases().filter(b => With.framesSince(b.lastFrameScoutedByUs) > Seconds(30)()),
      scoutableBases())
    val toScoutBaseSorted = toScoutBases.toVector.sortBy(b =>
      Maff.maxBy(units)(_.squadAge).map(_.framesToTravelTo(b.heart))
      .getOrElse(b.heart.groundTiles(With.geography.home)))

    vicinity        = toScoutBaseSorted.head.heart.center
    lock.counter    = CountOne
    lock.matcher    = ?(With.blackboard.wantToAttack() && With.scouting.enemyProximity < 0.65, matchAll, matchFlying)
    lock.preference = PreferClose(vicinity)
    lock.acquire()
  }

  def run(): Unit = {
    units.foreach(u => {
      val targetsHere   = u.base.toSeq.filter(_.isEnemy).flatMap(_.enemies)
      val targetsThere  = vicinity.base.toSeq.flatMap(_.enemies)
      u.intend(this)
        .setCanSneak(true)
        .setTerminus(vicinity)
        .setTargets(SquadAutomation.rankForArmy(
          this,
          (targetsHere ++ targetsThere)
            .filter(_.caughtBy(u))
            .toSet
            .toVector))
    })
  }
}