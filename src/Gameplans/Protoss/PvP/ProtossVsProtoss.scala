package Gameplans.Protoss.PvP

import Gameplans.All.ModalGameplan

class ProtossVsProtoss extends ModalGameplan(
  new PvPCannonRushFE,
  new PvPCannonRushMaximum,
  new PvPOpeningVsFE,
  new PvPNexusFirst,
  new PvPOpening,
  new PvPLateGame,
)