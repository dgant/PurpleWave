package Gameplans.Protoss.PvP

import Gameplans.All.ModalGameplan
import Gameplans.Protoss.PvE.{PvECannonRushFE, PvECannonRushMaximum}

class ProtossVsProtoss extends ModalGameplan(
  new PvECannonRushFE,
  new PvECannonRushMaximum,
  new PvPOpeningVsFE,
  new PvPNexusFirst,
  new PvPOpening,
  new PvPLateGame,
)