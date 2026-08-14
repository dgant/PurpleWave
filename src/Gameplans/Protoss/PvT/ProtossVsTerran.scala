package Gameplans.Protoss.PvT

import Gameplans.All.ModalGameplan
import Gameplans.Protoss.PvE.PvECannonRushMaximum

class ProtossVsTerran extends ModalGameplan(
  new PvECannonRushMaximum,
  new PvTFastCarrier,
  new PvTArbiter,
  new PvTDoubleRobo
)