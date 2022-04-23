package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Utilities.UnitCounters.CountEverything
import ProxyBwapi.Races.Protoss

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
    lock.units.foreach(u => u.intend(this, new Intention {
      toTravel = Some(partyCentral)
      shouldMeld = u.matchups.threatsInRange.isEmpty
    }))
  }
}
