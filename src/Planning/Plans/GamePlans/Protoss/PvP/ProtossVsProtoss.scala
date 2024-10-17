package Planning.Plans.Gameplans.Protoss.PvP

import Planning.Plans.Gameplans.All.ModalGameplan

class ProtossVsProtoss extends ModalGameplan(
  new PvPNexusFirst,
  new PvPOpening,
  new PvPLateGame,
)