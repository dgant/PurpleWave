package Planning.Plans

import Gameplans.All.Modal
import Lifecycle.With
import Utilities.Property
import bwapi.Race

class SwitchOurRace(
  whenTerran  : Plan = NoPlan(),
  whenProtoss : Plan = NoPlan(),
  whenZerg    : Plan = NoPlan())
    extends Plan with Modal {
  
  val terran  = new Property[Plan](whenTerran)
  val protoss = new Property[Plan](whenProtoss)
  val zerg    = new Property[Plan](whenZerg)

  def isComplete: Boolean = false

  override def onUpdate(): Unit = {
    val matchupPlan = With.self.raceInitial match {
      case Race.Terran    => terran
      case Race.Protoss   => protoss
      case Race.Zerg      => zerg
      case _              => throw new Exception("We're not Terran, Protoss, or Zerg. Are we Xel'naga?!")
    }
    matchupPlan.get.update()
  }
}
