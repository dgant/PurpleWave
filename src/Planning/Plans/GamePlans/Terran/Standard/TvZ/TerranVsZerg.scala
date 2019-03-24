package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TerranReactionVsWorkerRush

class TerranVsZerg extends ModalGameplan(
  new TerranReactionVsWorkerRush,
  new TvZ8Rax,
  new TvZ1RaxFE,
  new TvZ2RaxAcademy,
  new TvZSK
)