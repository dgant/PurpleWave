package Planning.Plans.GamePlans.Terran.TvR

import Planning.Plans.GamePlans.ModalGameplan

class TerranVsRandom extends ModalGameplan(
  new TvRTinfoil,
  new TvR1Rax
)
