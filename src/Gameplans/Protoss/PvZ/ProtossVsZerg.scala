package Gameplans.Protoss.PvZ

import Gameplans.All.ModalGameplan
import Gameplans.Protoss.PvE.PvECannonRushMaximum

class ProtossVsZerg extends ModalGameplan(
  new PvECannonRushMaximum,
  new PvZFE,
  new PvZ1BaseReactive,
  new PvZ2022
)