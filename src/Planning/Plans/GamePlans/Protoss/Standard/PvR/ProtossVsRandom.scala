package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Protoss.Standard.PvE.PvEProxy2Gate

class ProtossVsRandom extends ModalGameplan(
  new PvEProxy2Gate,
  new PvR2Gate910,
  new PvR2Gate1012,
  new PvRZCoreZ,
  new PvRZZCore,
  new PvRTinfoil
)