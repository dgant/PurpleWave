package Planning.Plans.Protoss.GamePlans.Standard

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.Protoss.GamePlans.Standard.PvP._

class ProtossVsProtoss extends ModalGameplan(
  new PvPOpen2Gate1012,
  new PvPOpen1GateGoonExpand,
  new PvPOpen1GateReaverExpand,
  new PvPOpen1015GateGoonExpand,
  new PvPOpen1015GateGoonReaverExpand,
  new PvPOpen1015GateGoonDTs,
  new PvPOpen12Nexus5Zealot,
  new PvPOpen2GateRobo,
  new PvPOpen2GateDarkTemplar,
  new PvPOpen3GateSpeedlots,
  new PvPOpen4GateGoon,
  new PvPLateGame
)