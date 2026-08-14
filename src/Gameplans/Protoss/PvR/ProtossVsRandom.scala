package Gameplans.Protoss.PvR

import Gameplans.All.ModalGameplan
import Gameplans.Protoss.PvE.PvECannonRushMaximum

class ProtossVsRandom extends ModalGameplan(
  new PvECannonRushMaximum,
  new PvR2Gate4Gate
)