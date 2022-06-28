package Planning.Plans.GamePlans.Terran.TvT

import Planning.Plans.GamePlans.All.ModalGameplan
import Planning.Plans.GamePlans.Terran.TvE.TerranReactionVsWorkerRush

class TerranVsTerran extends ModalGameplan(
  new TerranReactionVsWorkerRush,
  new TvT14CC,
  new TvT1RaxFE,
  new TvT1FacFE,
  new TvT1FacPort,
  new TvT2FacTanks,
  new TvT2Port,
  new TvT2Base2Port,
  new TvT2BaseBC,
  new TvTLateGame // TvT5Fac, or late-game for strategies
)