package Planning.Plans.GamePlans.Terran.Standard.TvP

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TvTPNukeDrop

class TerranVsProtoss extends ModalGameplan(
  new TvPJoyO,
  new TvP14CC,
  new TvPFDStrong,
  new TvPMidgameBioTank,
  new TvTPNukeDrop
)