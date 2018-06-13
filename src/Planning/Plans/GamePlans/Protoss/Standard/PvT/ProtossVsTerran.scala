package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsTerran extends ModalGameplan(
  new PvT1015Expand,
  new PvT1015GateDT,
  new PvTStove,
  new PvTBasic
)