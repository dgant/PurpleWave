package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsRandom extends ModalGameplan(
  new PvR2Gate910,
  new PvR2Gate1012,
  new PvRZCoreZ,
  new PvRDT,
  new PvRTinfoil
)