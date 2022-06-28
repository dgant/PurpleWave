package Planning.Plans.GamePlans.Protoss.PvP

import Planning.Plans.GamePlans.All.ModalGameplan

class ProtossVsProtoss extends ModalGameplan(
  new PvPOpening,
  new PvPLateGame,
)