package Planning.Plans.GamePlans.Terran.Standard.TvZ

import Planning.Plans.GamePlans.ModalGameplan

class TerranVsZerg extends ModalGameplan(
  new TvZCCFirst,
  new TvZ1RaxFEEconomic,
  new TvZ1RaxFEConservative,
  new TvZ2RaxExpand,
  new TvZ1RaxGas,
  new TerranVsZergBio,
  new TerranVsZergMech
)