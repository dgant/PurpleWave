package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsProtoss extends ModalGameplan(
  new PvP2Gate1012,
  new PvP1GateReaverExpand,
  new PvP12Nexus5Zealot,
  new PvP2GateRobo,
  new PvP2GateDarkTemplar,
  new PvP3GateSpeedlots,
  new PvP4GateGoon,
  new PvPLateGame
)