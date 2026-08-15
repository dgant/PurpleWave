package Gameplans.Protoss.PvT

import Gameplans.All.ModalGameplan

class ProtossVsTerran extends ModalGameplan(
  new PvTSpeedlotDT,
  new PvTFastCarrier,
  new PvTArbiter,
  new PvTDoubleRobo
)