package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Planning.Plans.Compound._
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Strategy.{Employing, StartPositionsAtLeast}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvZ2Gate1012

class PvZ2Gate1012 extends PvZ1Base {
  override val activationCriteria = new Employing(PvZ2Gate1012)
  override val scoutPlan   = new If(new StartPositionsAtLeast(4), new ScoutOn(Protoss.Pylon), new ScoutOn(Protoss.Gateway))
  override def buildOrder         = ProtossBuilds.TwoGate1012
}

