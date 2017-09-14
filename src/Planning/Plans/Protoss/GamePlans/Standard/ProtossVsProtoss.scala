package Planning.Plans.Protoss.GamePlans.Standard

import Planning.Plans.GamePlans.ModalGamePlan
import Planning.Plans.Macro.BuildOrders.RequireBareMinimum
import Planning.Plans.Protoss.GamePlans.Standard.PvP.{PvPLateGameStandard, PvPOpen1GateRoboObs, PvPOpen2GateDarkTemplar, PvPOpen2GateRoboObs}

class ProtossVsProtoss extends ModalGamePlan(
  new RequireBareMinimum,
  new PvPOpen1GateRoboObs,
  new PvPOpen2GateRoboObs,
  new PvPOpen2GateDarkTemplar,
  new PvPLateGameStandard
)