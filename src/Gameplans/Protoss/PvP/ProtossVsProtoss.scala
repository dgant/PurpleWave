package Gameplans.Protoss.PvP

import Gameplans.All.ModalGameplan

class ProtossVsProtoss extends ModalGameplan(
  new PvPCannonRush,
  new PvPOpeningVsFE,
  new PvPNexusFirst,
  new PvPOpening,
  new PvPLateGame,
)