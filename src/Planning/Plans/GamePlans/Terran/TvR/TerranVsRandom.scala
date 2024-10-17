package Planning.Plans.Gameplans.Terran.TvR

import Planning.Plans.Gameplans.All.ModalGameplan

class TerranVsRandom extends ModalGameplan(
  new TvRTinfoil,
  new TvR1Rax
)
