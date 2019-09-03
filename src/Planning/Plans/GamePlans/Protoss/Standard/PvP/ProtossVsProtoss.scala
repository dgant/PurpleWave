package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsProtoss extends ModalGameplan(
  new PvPVsForge,
  new PvP2Gate1012Expand,
  new PvP2Gate1012GoonOrDT,
  new PvPRobo,
  new PvP2GateDarkTemplar,
  new PvP2GateGoon,
  new PvP3GateGoon,
  new PvP4GateGoon,
  new PvPMidGame,
  // new PvPLateGame
  new PvPLateGame2
)