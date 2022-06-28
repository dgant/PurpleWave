package Planning.Plans.GamePlans.Terran.TvR

import Planning.Plans.GamePlans.All.ModalGameplan

class TerranVsRandom extends ModalGameplan(
  new TvRTinfoil,
  new TvR1Rax
)
