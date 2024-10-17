package Planning.Plans.Gameplans.Zerg.ZvT

import Planning.Plans.Gameplans.All.ModalGameplan
import Planning.Plans.Gameplans.Zerg.ZvE.ZergReactionVsWorkerRush

class ZergVsTerran extends ModalGameplan(
  new ZergReactionVsWorkerRush,
  new ZvT1HatchHydra,
  new ZvT1HatchLurker,
  new ZvT2HatchLingBustMuta,
  new ZvT2HatchLurker,
  new ZvT3HatchLing,
  new ZvT13PoolMuta,
  //new ZvTProxyHatch,

  new ZvT7Pool,
  new ZvTOpening,
  new ZvTMidgame,
  new ZvTLateGame
)