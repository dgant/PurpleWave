package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsProtoss extends ModalGameplan(
  new PvPVsForge,
  new PvPOpening,
  new PvPLateGame,
)