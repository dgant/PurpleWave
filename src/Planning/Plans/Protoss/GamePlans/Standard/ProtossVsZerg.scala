package Planning.Plans.Protoss.GamePlans.Standard

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.Protoss.GamePlans.Specialty.PvZ4GateGoon

class ProtossVsZerg extends ModalGameplan(
  new PvZ4GateGoon,
  new ProtossVsZergOld
)