package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsZerg extends ModalGameplan(
  new PvZ4Gate99,
  new PvZ4Gate,
  new PvZFFE,
  new ProtossVsZergSpeedlotTemplar,
  new ProtossVsZerg8Gate
)