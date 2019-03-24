package Planning.Plans.GamePlans.Zerg.ZvT

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush

class ZergVsTerran extends ModalGameplan(
  new ZergReactionVsWorkerRush,
  new ZvT1HatchHydra,
  new ZvT1HatchLurker,
  new ZvT2HatchLingBustMuta,
  new ZvT2HatchLurker,
  new ZvT3HatchLing,
  new ZvT13PoolMuta,
  new ZvTProxyHatch
)