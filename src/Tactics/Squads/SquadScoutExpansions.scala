package Tactics.Squads

import Lifecycle.With
import Mathematics.Maff
import Performance.Cache
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers._
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.{GameTime, Seconds}

class SquadScoutExpansions extends Squad {

  def frameToScout: GameTime = {
    if (With.self.isProtoss) {
      if (With.enemy.isTerran)        GameTime(9, 0)
      else if (With.enemy.isProtoss)  GameTime(9, 0)
      else                            GameTime(7, 0)
    } else if (With.self.isTerran) {
      if (With.enemy.isTerran)        GameTime(8, 0)
      else if (With.enemy.isProtoss)  GameTime(7, 0)
      else                            GameTime(7, 0)
    } else {
      if (With.enemy.isTerran)        GameTime(9, 0)
      else if (With.enemy.isProtoss)  GameTime(9, 0)
      else                            GameTime(12, 0)
    }
  }

  lock.counter = CountOne
  lock.matcher = MatchOr(
    Terran.Marine,
    Terran.Firebat,
    Terran.Vulture,
    Terran.Goliath,
    Terran.Wraith,
    Protoss.Zealot,
    Protoss.Dragoon,
    Protoss.DarkTemplar,
    Protoss.Observer,
    Zerg.Zergling,
    Zerg.Hydralisk,
    Zerg.Scourge)

  val scoutableBases = new Cache(() =>
    With.geography.neutralBases
      .view
      .filterNot(With.scouting.enemyMain.contains)
      .filterNot(With.scouting.enemyNatural.contains)
      .filterNot(b =>
        With.scouting.enemyMain.exists(m =>
          m.metro == b.metro
          && With.scouting.enemyNatural.exists(n =>
            b.heart.tileDistanceGroundManhattan(m.heart) < n.heart.tileDistanceGroundManhattan(m.heart))))
      .toVector
      .sortBy(base => -With.scouting.baseIntrigue.getOrElse(base, 0.0))
      .sortBy(b => lock.units.exists(_.flying) || b.zone.island))

  def launch(): Unit = {
    if (With.frame < frameToScout()) return
    if (With.geography.ourBases.size < 2) return
    if ( ! With.blackboard.wantToAttack() && ! With.blackboard.wantToHarass()) return
    if (scoutableBases().isEmpty) return

    val toScoutBases = Maff.orElse(
      scoutableBases().filter(b => With.framesSince(b.lastScoutedFrame) > Seconds(90)()),
      scoutableBases().filter(b => With.framesSince(b.lastScoutedFrame) > Seconds(30)()),
      scoutableBases())
    val toScoutBaseSorted = toScoutBases.toVector.sortBy(b =>
      Maff.maxBy(units)(_.squadAge).map(_.framesToTravelTo(b.heart))
      .getOrElse(b.heart.tileDistanceGroundManhattan(With.geography.home)))

    vicinity = toScoutBaseSorted.head.townHallArea.center

    lock.preference = PreferClose(vicinity)
    addUnits(lock.acquire())
  }

  def run(): Unit = {
    SquadAutomation.send(this)
    units.foreach(u => {
      u.intent.canFight = u.visibleToOpponents && u.matchups.threatsInRange.nonEmpty
    })
  }
}