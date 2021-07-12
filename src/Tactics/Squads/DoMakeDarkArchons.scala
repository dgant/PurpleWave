package Tactics.Squads

import Lifecycle.With
import Micro.Agency.Intention
import Planning.UnitCounters.CountEverything
import ProxyBwapi.Races.Protoss

class DoMakeDarkArchons extends Squad {

  lock.matcher = Protoss.DarkTemplar
  lock.counter = CountEverything

  def recruit(): Unit = {
    if (With.blackboard.makeDarkArchons()) {
      lock.release() // Ditch units that are already Dark Archons
      lock.acquire(this)
    }
  }

  override def run(): Unit = {
    units.foreach(_.intend(this, new Intention {
      toTravel = Some(With.geography.home.center)
      shouldMeld = true
    }))
  }
}
