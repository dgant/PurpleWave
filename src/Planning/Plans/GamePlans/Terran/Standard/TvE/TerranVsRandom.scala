package Planning.Plans.GamePlans.Terran.Standard.TvE

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Terran.Standard.TvR.{TvR1Rax, TvRTinfoil}

class TerranVsRandom extends ModalGameplan(
  new TvR1Rax,
  new TvRTinfoil
)
