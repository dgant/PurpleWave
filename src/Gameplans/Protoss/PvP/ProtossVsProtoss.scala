package Gameplans.Protoss.PvP

import Gameplans.All.ModalGameplan

class ProtossVsProtoss extends ModalGameplan(
  new PvPCannonRush,
  new PvPNexusFirst,
  new PvPOpening,
  new PvPLateGame,
)