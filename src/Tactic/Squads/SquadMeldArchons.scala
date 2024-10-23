package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.Protoss

class SquadMeldArchons extends Squad {

  def launch(): Unit = {
    val threshold = With.blackboard.maximumArchonEnergy()
    lock.release() // Ditch units that are already Archons
    if (threshold > 0) {
      lock.matcher = u => Protoss.HighTemplar(u) && u.energy < threshold
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
