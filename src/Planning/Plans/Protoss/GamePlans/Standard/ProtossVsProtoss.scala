package Planning.Plans.Protoss.GamePlans.Standard

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.Protoss.GamePlans.Standard.PvP.{PvPLateGameStandard, PvPOpen1GateRoboObs, PvPOpen2GateDarkTemplar, PvPOpen2GateRobo}

class ProtossVsProtoss extends ModalGameplan(
  new PvPOpen1GateRoboObs,
  new PvPOpen2GateRobo,
  new PvPOpen2GateDarkTemplar,
  new PvPLateGameStandard
)