package Planning.Plans.Protoss.GamePlans.Standard

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.Protoss.GamePlans.Specialty.PvZFourGateAllIn

class ProtossVsZerg extends ModalGameplan(
  new PvZFourGateAllIn,
  new ProtossVsZergOld
)