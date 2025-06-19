package Gameplans.Protoss.PvT

import Gameplans.All.ModalGameplan

class ProtossVsTerran extends ModalGameplan(
  new PvTFastCarrier,
  new PvTArbiter,
  new PvTDoubleRobo
)