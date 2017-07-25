package Planning.Plans.Information

import Lifecycle.With
import Planning.Composition.Property
import Planning.Plan
import bwapi.Race

class SwitchOurRace(
  whenTerran  : Plan = new Plan,
  whenProtoss : Plan = new Plan,
  whenZerg    : Plan = new Plan)
    extends Plan {
  
  val terran  = new Property[Plan](whenTerran)
  val protoss = new Property[Plan](whenProtoss)
  val zerg    = new Property[Plan](whenZerg)
  
  description.set("Given our race")
  
  override def getChildren: Iterable[Plan] = Vector(terran.get, protoss.get, zerg.get)
  override def onUpdate() {
    
    val matchupPlan = With.self.race match {
      case Race.Terran    => terran
      case Race.Protoss   => protoss
      case Race.Zerg      => zerg
      case _              => throw new Exception("We're not Terran, Protoss, or Zerg. Are we Xel'naga?!")
    }
  
    delegate(matchupPlan.get)
  }
}
