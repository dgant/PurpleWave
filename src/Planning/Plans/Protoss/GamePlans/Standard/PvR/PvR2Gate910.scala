package Planning.Plans.Protoss.GamePlans.Standard.PvR

import Planning.Plans.Information.Employing
import Planning.Plans.Protoss.ProtossBuilds
import Strategery.Strategies.Protoss.PvR.PvREarly2Gate910

class PvR2Gate910 extends PvR2Gate1012 {
  override val activationCriteria = new Employing(PvREarly2Gate910)
  override val buildOrder         = ProtossBuilds.OpeningTwoGate910
}
