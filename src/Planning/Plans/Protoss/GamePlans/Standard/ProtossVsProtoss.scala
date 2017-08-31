package Planning.Plans.Protoss.GamePlans.Standard

import Planning.Plans.GamePlans.ModalGamePlan
import Planning.Plans.Protoss.GamePlans.Standard.PvP.{PvPLateGameStandard, PvPOpen1GateRoboObs, PvPOpen2GateRoboObs, PvPOpen2GateDarkTemplar}

class ProtossVsProtoss extends ModalGamePlan(
  new PvPOpen1GateRoboObs,
  new PvPOpen2GateRoboObs,
  new PvPOpen2GateDarkTemplar,
  new PvPLateGameStandard
)