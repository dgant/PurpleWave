package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZ4Gate99

class PvZ4Gate99 extends PvZ4Gate {
  override val activationCriteria = new Employing(PvZ4Gate99)
  override def scoutPlan = new ScoutOn(Protoss.Gateway, quantity = 2)
  override def buildOrder = ProtossBuilds.TwoGate899
}
