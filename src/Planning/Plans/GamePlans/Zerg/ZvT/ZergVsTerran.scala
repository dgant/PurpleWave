package Planning.Plans.GamePlans.Zerg.ZvT

import Planning.Plans.GamePlans.ModalGameplan

class ZergVsTerran extends ModalGameplan(
  new ZvT1HatchHydra,
  new ZvT1HatchLurker,
  new ZvT2HatchLingBustMuta,
  new ZvT2HatchLurker,
  new ZvT3HatchLing,
  new ZvT13PoolMuta,
  new ZvTProxyHatch
)