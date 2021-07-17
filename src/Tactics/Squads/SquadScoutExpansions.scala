package Tactics.Squads

import Lifecycle.With
import Performance.Cache
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers._
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.GameTime

class SquadScoutExpansions extends Squad {

  def frameToScout: GameTime = {
    if (With.self.isProtoss) {
      if (With.enemy.isTerran)        GameTime(9, 0)
      else if (With.enemy.isProtoss)  GameTime(10, 0)
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
    Zerg.Zergling,
    Zerg.Hydralisk,
    Zerg.Scourge)

  val scoutableBases = new Cache(() =>
    With.geography.neutralBases
      .view
      .filterNot(With.scouting.enemyMain.contains)
      .filterNot(With.scouting.enemyNatural.contains)
      .filterNot(b => With.scouting.enemyMain.exists(_.metro == b.metro))
      .toVector
      .sortBy(base => -With.scouting.baseIntrigue.getOrElse(base, 0.0))
      .sortBy(_.zone.island))

  def launch(): Unit = {
    if (With.frame < frameToScout()) return
    if (With.geography.ourBases.size < 2) return
    if ( ! With.blackboard.wantToAttack() &&  ! With.blackboard.wantToHarass()) return

    if (scoutableBases().isEmpty) return
    vicinity = scoutableBases().headOption.map(_.townHallArea.center).getOrElse(With.geography.home.center)
    lock.preference = PreferClose(vicinity)
    addUnits(lock.acquire(this))
  }

  def run(): Unit = {
    SquadAutomation.send(this)
  }
}