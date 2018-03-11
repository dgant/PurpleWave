package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsProtoss extends ModalGameplan(
  new PvP2Gate1012,
  new PvP1GateGoonExpand,
  new PvP1GateReaverExpand,
  new PvP1015GateGoonExpand,
  new PvPOpen1015GateGoonReaverExpand,
  new PvPOpen1015GateGoonDTs,
  new PvP12Nexus5Zealot,
  new PvP2GateRobo,
  new PvPOpen2GateDarkTemplar,
  new PvPOpen3GateSpeedlots,
  new PvPOpen4GateGoon,
  new PvPLateGame
)