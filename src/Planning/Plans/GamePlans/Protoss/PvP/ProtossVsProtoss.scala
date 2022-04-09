package Planning.Plans.GamePlans.Protoss.PvP

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsProtoss extends ModalGameplan(
  new PvPOpening,
  new PvPLateGame,
)