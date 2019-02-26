package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsProtoss extends ModalGameplan(
  new PvPVsForge,
  new PvP2Gate1012Expand,
  new PvP2Gate1012Goon,
  new PvP1GateReaverExpand,
  new PvPGateGateRobo,
  new PvP2GateGoon,
  new PvP2GateDarkTemplar,
  new PvP3GateRobo,
  new PvP3GateGoon,
  new PvP4GateGoon,
  new PvPLateGame
)