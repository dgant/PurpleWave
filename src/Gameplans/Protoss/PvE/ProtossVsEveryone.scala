package Gameplans.Protoss.PvE

import Gameplans.All.ModalGameplan

class ProtossVsEveryone extends ModalGameplan(
  new PvECannonRushMaximum,
  new PvECannonRushFE,
  new PvEProxy2Gate
)
