package Tactics.Squads

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Performance.Cache
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers._
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.GameTime

class SquadScoutExpansions extends Squad with Prioritized {

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

  val scoutLock = new LockUnits(this)
  scoutLock.counter = CountOne
  scoutLock.matcher = MatchOr(
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

  def destination: Pixel = scoutableBases().headOption.map(_.townHallArea.center).getOrElse(With.geography.home.center)

  def recruit(): Unit = {
    if (With.frame < frameToScout()) return
    if (With.geography.ourBases.size < 2) return
    if (!With.blackboard.wantToAttack() && !With.blackboard.wantToHarass()) return

    if (scoutableBases().isEmpty) return
    scoutLock.preference = PreferClose(destination)
    val scouts = scoutLock.acquire(this)
    if (scouts.isEmpty) return
    scouts.foreach(addUnit)
  }

  def run(): Unit = {
    if (units.isEmpty) return
    targetQueue = Some(SquadTargeting.enRouteTo(units, destination))
    units.foreach(_.intend(this, new Intention {
      toTravel = Some(destination)
    }))
  }
}