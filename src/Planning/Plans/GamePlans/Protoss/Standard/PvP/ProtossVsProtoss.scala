package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsProtoss extends ModalGameplan(
  new PvPVsForge,
  new PvP1ZealotExpand,
  new PvPRobo,
  new PvP2GateDT,
  new PvP34GateGoon,
  new PvPLateGame,
)