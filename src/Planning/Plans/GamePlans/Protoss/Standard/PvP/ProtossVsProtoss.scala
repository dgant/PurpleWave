package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsProtoss extends ModalGameplan(
  new PvP2Gate1012Expand,
  new PvP2Gate1012Goon,
  new PvP1GateReaverExpand,
  new PvP2GateRobo,
  new PvP2GateDarkTemplar,
  new PvP3GateGoon,
  new PvP4GateGoon,
  new PvP2BaseReaverCarrier,
  new PvPLateGame
)