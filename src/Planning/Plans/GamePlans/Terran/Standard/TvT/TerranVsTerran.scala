package Planning.Plans.GamePlans.Terran.Standard.TvT

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TvTPNukeDrop

class TerranVsTerran extends ModalGameplan(
  new TvTPNukeDrop,
  new TerranVsTerranOld
)