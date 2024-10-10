package Planning.Plans.GamePlans.Protoss.PvP

import Lifecycle.With
import Placement.Access.PlaceLabels.DefendEntrance
import Placement.Access.PlacementQuery
import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.GamePlans.Protoss.PvP.PvPIdeas.requireTimelyDetection
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPNexusFirst

class PvPNexusFirst extends GameplanImperative {

  override def activated: Boolean = employing(PvPNexusFirst)
  override def completed: Boolean = {
    _complete ||= have(Protoss.RoboticsFacility)
    _complete ||= units(Protoss.Gateway) >= 5
      _complete
  }
  private var _complete: Boolean = false

  private def fearMuscle  = ! enemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.forgeFe)
  private def fearDT      = ! enemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.robo, With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon) || enemyStrategy(With.fingerprints.dtRush)

  override def executeBuild(): Unit = {
    scoutOn(Protoss.Gateway)

    once(8, Protoss.Probe)
    get(Protoss.Pylon)
    once(13, Protoss.Probe)
    get(2, Protoss.Nexus)
    once(14, Protoss.Probe)
    get(Protoss.Gateway)
    once(15, Protoss.Probe)
    if ( ! fearMuscle || With.fingerprints.dtRush()) {
      get(Protoss.Pylon, new PlacementQuery(Protoss.Pylon).preferBase(With.geography.ourFoyer).preferLabelYes(DefendEntrance))
    }
    once(16, Protoss.Probe)
    get(Protoss.Assimilator)
    once(17, Protoss.Probe)
    if (fearMuscle) {
      once(Protoss.Zealot)
    }
    get(Protoss.CyberneticsCore)
  }

  override def executeMain(): Unit = {

    if (fearMuscle) {
      get(2, Protoss.Gateway)
      once(2, Protoss.Zealot)
    }
    if (fearDT) {
      get(Protoss.Forge)
      requireTimelyDetection()
    }
    pump(Protoss.Dragoon)
    if ( ! fearMuscle) {
      get(Protoss.RoboticsFacility)
    }
    get(Protoss.DragoonRange)
    get(5, Protoss.Gateway)
    pump(Protoss.Zealot)
    get(2, Protoss.Assimilator)
  }
}
