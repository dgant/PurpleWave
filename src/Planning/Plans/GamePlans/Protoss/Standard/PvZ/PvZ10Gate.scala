package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.Get
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZ10Gate

class PvZ10Gate extends PvZ1Base {
  override val activationCriteria = new Employing(PvZ10Gate)
  override val scoutPlan = new ScoutOn(Protoss.Gateway)
  override def buildOrder = Vector(
    Get(8, Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(12, Protoss.Probe))
}
