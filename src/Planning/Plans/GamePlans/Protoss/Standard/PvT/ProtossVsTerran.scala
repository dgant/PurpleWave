package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsTerran extends ModalGameplan(
  new PvT1015GateDT,
  new PvT4Gate,
  new PvT1GateReaver,
  new PvT1GateStargate,
  new PvTStove,
  new PvTBasic
)