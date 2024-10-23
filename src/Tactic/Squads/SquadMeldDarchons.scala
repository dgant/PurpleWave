package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.Protoss

class SquadMeldDarchons extends Squad {

  lock.matcher = Protoss.DarkTemplar

  def launch(): Unit = {
    lock.release() // Ditch units that are already Dark Archons
    if (With.blackboard.makeDarkArchons()) {
      lock.acquire()
    }
  }

  override def run(): Unit = {
    val partyCentral = Maff.minBy(With.geography.ourBases)(_.groundPixels(With.scouting.ourMuscleOrigin)).map(_.heart).getOrElse(With.geography.home).center
    lock.units.foreach(_.intend(this)
      .setShouldMeld(true)
      .setTerminus(partyCentral))
  }
}
