package Planning.Plans.Protoss.GamePlans.Standard

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.Protoss.GamePlans.Standard.PvT._

class ProtossVsTerran extends ModalGameplan(
  new PvT13Nexus,
  new PvT1GateRange,
  new PvT1015GateExpand,
  new PvT1GateProxy,
  new PvTDTExpand,
  new PvT1GateStargate,
  new PvTStove,
  new PvT2BaseArbiters,
  new PvT2BaseCarriers,
  new PvT2BaseGateways,
  new PvT2BaseGatewaysForever,
  new PvT3BaseCorsairs,
  new PvT3BaseArbiters,
  new PvT3BaseCarriers
)