package Planning.Plans.GamePlans.Protoss.PvT

import Planning.Plans.GamePlans.All.ModalGameplan

class ProtossVsTerran extends ModalGameplan(
  new PvT1015Expand,
  new PvT1015DT,
  new PvTStove,
  new PvTBasic
)