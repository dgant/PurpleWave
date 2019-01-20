package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Planning.Plans.GamePlans.ModalGameplan

class TerranVsZerg extends ModalGameplan(
  new TvZ8Rax,
  new TvZ1RaxFE,
  new TvZ2RaxAcademy,
  new TvZSK
)