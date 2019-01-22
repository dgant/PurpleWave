package Planning.Plans.GamePlans.Terran.Standard.TvT

import Planning.Plans.GamePlans.ModalGameplan

class TerranVsTerran extends ModalGameplan(
  new TvT14CC,
  new TvT1RaxFE,
  new TvT1FacFE,
  new TvT1FacPort,
  new TvT2FacTanks,
  new TvT2Port,
  new TvT2Base2Port,
  new TvT5Fac
)