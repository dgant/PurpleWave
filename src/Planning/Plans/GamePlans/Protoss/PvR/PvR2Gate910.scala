package Planning.Plans.Gameplans.Protoss.PvR

import Planning.Plans.Gameplans.Protoss.ProtossBuilds
import Planning.Predicates.Strategy.Employing
import Strategery.Strategies.Protoss.PvROpen2Gate910

class PvR2Gate910 extends PvR2Gate1012 {
  
  override val activationCriteria = new Employing(PvROpen2Gate910)
  override val buildOrder         = ProtossBuilds.TwoGate910
}
