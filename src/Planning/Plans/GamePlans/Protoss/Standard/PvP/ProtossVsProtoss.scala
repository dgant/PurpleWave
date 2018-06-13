package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsProtoss extends ModalGameplan(
  new PvP2Gate1012,
  new PvP1GateReaverExpand,
  new PvP2GateRobo,
  new PvP2GateDarkTemplar,
  new PvP4GateGoon,
  new PvPLateGame
)