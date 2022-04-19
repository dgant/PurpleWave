package Planning.Plans.Compound

import Lifecycle.With
import Planning.Plans.Basic.NoPlan
import Planning.Plan
import Utilities.Property
import bwapi.Race

class SwitchOurRace(
  whenTerran  : Plan = NoPlan(),
  whenProtoss : Plan = NoPlan(),
  whenZerg    : Plan = NoPlan())
    extends Plan {
  
  val terran  = new Property[Plan](whenTerran)
  val protoss = new Property[Plan](whenProtoss)
  val zerg    = new Property[Plan](whenZerg)

  override def onUpdate() {
    val matchupPlan = With.self.raceInitial match {
      case Race.Terran    => terran
      case Race.Protoss   => protoss
      case Race.Zerg      => zerg
      case _              => throw new Exception("We're not Terran, Protoss, or Zerg. Are we Xel'naga?!")
    }
    matchupPlan.get.update()
  }
}
