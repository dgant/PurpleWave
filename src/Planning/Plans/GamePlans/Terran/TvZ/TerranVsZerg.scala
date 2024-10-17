package Planning.Plans.Gameplans.Terran.TvZ

import Planning.Plans.Gameplans.All.ModalGameplan
import Planning.Plans.Gameplans.Terran.TvE.TerranReactionVsWorkerRush

class TerranVsZerg extends ModalGameplan(
  new TerranReactionVsWorkerRush,
  new TvZ8Rax,
  new TvZ1RaxFE,
  new TvZ2RaxAcademy,
  new TvZSK
)