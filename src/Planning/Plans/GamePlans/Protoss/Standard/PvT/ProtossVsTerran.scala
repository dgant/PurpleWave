package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsTerran extends ModalGameplan(
  new PvT13Nexus,
  new PvT1GateRange,
  new PvT1015GateDT,
  new PvT1015GateExpand,
  new PvT1015GatePressure,
  new PvT4Gate,
  new PvT1GateProxy,
  new PvTDTDrop,
  new PvTDTExpand,
  new PvT1GateReaver,
  new PvT1GateStargate,
  new PvTStove,
  new PvT1BaseCarrier,
  new PvT2BaseArbiters,
  new PvT2BaseCarriers,
  new PvT2BaseGateways,
  new PvT2BaseGatewaysForever,
  new PvTFastThird,
  new PvT3BaseCorsairs,
  new PvT3BaseArbiters,
  new PvT3BaseCarriers
)