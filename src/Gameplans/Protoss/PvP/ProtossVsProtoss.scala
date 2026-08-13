package Gameplans.Protoss.PvP

import Gameplans.All.ModalGameplan
import Gameplans.Protoss.PvR.PvRCannonRushMaximum

class ProtossVsProtoss extends ModalGameplan(
  new PvPCannonRushFE,
  new PvRCannonRushMaximum,
  new PvPOpeningVsFE,
  new PvPNexusFirst,
  new PvPOpening,
  new PvPLateGame,
)