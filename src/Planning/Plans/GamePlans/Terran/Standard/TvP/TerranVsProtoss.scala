package Planning.Plans.GamePlans.Terran.Standard.TvP

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TvTPNukeDrop

class TerranVsProtoss extends ModalGameplan(

  new TvP14CC,
  new TvPSiegeExpandBunker,
  new TvPFDStrong,
  new TvPJoyO,
  new TvPDeep4,
  new TvP6Fac,
  new TvP2Armory,
  new TvTPNukeDrop
)