package Gameplans.Protoss.PvZ

import Gameplans.All.ModalGameplan

class ProtossVsZerg extends ModalGameplan(
  new PvZFE,
  new PvZ1BaseReactive,
  new PvZ2022
)