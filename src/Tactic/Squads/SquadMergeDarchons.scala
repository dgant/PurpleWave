package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.Protoss
import Utilities.UnitCounters.CountEverything

class SquadMergeDarchons extends Squad {

  lock.matcher = Protoss.DarkTemplar
  lock.counter = CountEverything

  def launch(): Unit = {
    lock.release() // Ditch units that are already Dark Archons
    if (With.blackboard.makeDarkArchons()) {
      lock.acquire()
    }
  }

  override def run(): Unit = {
    val partyCentral = Maff.maxBy(With.geography.ourBases)(_.heart.tileDistanceSquared(With.scouting.enemyThreatOrigin)).map(_.heart).getOrElse(With.geography.home).center
    lock.units.foreach(_.intend(this)
      .setShouldMeld(true)
      .setTravel(partyCentral))
  }
}
