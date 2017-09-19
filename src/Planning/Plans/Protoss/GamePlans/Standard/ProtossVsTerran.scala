package Planning.Plans.Protoss.GamePlans.Standard

import Planning.Plans.GamePlans.ModalGamePlan
import Planning.Plans.Protoss.GamePlans.Standard.PvT._

class ProtossVsTerran extends ModalGamePlan(
  new PvT13Nexus,
  new PvT1GateRange,
  new PvT1015Gate,
  new PvTDTExpand,
  new PvT2BaseArbiters,
  new PvT2BaseCarriers,
  new PvT2BaseGateway,
  new PvT3BaseCorsair,
  new PvT3BaseArbiters,
  new PvT3BaseCarriers
)