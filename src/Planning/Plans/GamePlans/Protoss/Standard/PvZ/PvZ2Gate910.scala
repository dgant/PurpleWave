package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZ2Gate910

class PvZ2Gate910 extends PvZ2Gate1012 {
  override val activationCriteria = new Employing(PvZ2Gate910)
  override val scoutPlan          = new ScoutOn(Protoss.Gateway, quantity = 2)
  override def buildOrder         = ProtossBuilds.TwoGate8910
}
