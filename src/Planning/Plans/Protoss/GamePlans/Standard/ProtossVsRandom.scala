package Planning.Plans.Protoss.GamePlans.Standard

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.Protoss.GamePlans.Specialty.Proxy2Gate
import Planning.Plans.Protoss.GamePlans.Standard.PvR._

class ProtossVsRandom extends ModalGameplan(
  new Proxy2Gate,
  new PvR2Gate910,
  new PvR2Gate1012,
  new PvRZCoreZ,
  new PvRZZCore,
  new PvRTinfoil
)