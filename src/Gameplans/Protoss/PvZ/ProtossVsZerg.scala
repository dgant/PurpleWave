package Gameplans.Protoss.PvZ

import Gameplans.All.ModalGameplan
import Gameplans.Protoss.PvR.PvRCannonRushMaximum

class ProtossVsZerg extends ModalGameplan(
  new PvRCannonRushMaximum,
  new PvZFE,
  new PvZ1BaseReactive,
  new PvZ2022
)