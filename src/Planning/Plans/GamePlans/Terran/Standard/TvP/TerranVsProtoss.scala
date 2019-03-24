package Planning.Plans.GamePlans.Terran.Standard.TvP

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Terran.Standard.TvE.TerranReactionVsWorkerRush

class TerranVsProtoss extends ModalGameplan(
  new TerranReactionVsWorkerRush,
  new TvP1RaxFE, // Comes first because it's also the gas steal reaction
  new TvPSiegeExpandBunker,
  new TvPFDStrong,
  new TvP2FacJoyO,
  new TvPDeep4,
  new TvP6Fac,
  new TvP2Armory
)