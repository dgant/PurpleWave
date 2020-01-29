package Planning.Plans.GamePlans.Terran.Standard.TvR

import Planning.Plans.GamePlans.ModalGameplan

class TerranVsRandom extends ModalGameplan(
  new TvRTinfoil,
  new TvR1Rax
)
