package Gameplans.Protoss.PvR

import Gameplans.All.ModalGameplan

class ProtossVsRandom extends ModalGameplan(
  new PvRCannonRushMaximum,
  new PvR2Gate4Gate
)