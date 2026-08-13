package Gameplans.Protoss.PvT

import Gameplans.All.ModalGameplan
import Gameplans.Protoss.PvR.PvRCannonRushMaximum

class ProtossVsTerran extends ModalGameplan(
  new PvRCannonRushMaximum,
  new PvTFastCarrier,
  new PvTArbiter,
  new PvTDoubleRobo
)