package Gameplans.Protoss.PvZ

import Gameplans.All.ModalGameplan
import Gameplans.Protoss.PvR.PvR2Gate4Gate

class ProtossVsZerg extends ModalGameplan(
  new PvZ1GateCoreExpand,
  new PvZ1GateCoreYoloSpeedlot,
  new PvZ1GateCoreYoloDragoon,
  new PvZFE,
  new PvZ1BaseReactive,
  new PvR2Gate4Gate
)