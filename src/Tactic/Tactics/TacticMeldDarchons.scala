package Tactic.Tactics

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.Protoss

class TacticMeldDarchons extends Tactic {

  lock.matcher = Protoss.DarkTemplar

  def launch(): Unit = {
    lock.release() // Ditch units that are already Dark Archons
    if (With.blackboard.makeDarkArchons()) {
      lock.acquire()
      val partyCentral = Maff.minBy(With.geography.ourBases)(_.groundPixels(With.scouting.ourMuscleOrigin)).map(_.heart).getOrElse(With.geography.home).center
      units.foreach(_.intend(this)
        .setShouldMeld(true)
        .setTerminus(partyCentral))
    }
  }
}
