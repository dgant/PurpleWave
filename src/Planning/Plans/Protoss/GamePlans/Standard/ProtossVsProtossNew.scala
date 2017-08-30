package Planning.Plans.Protoss.GamePlans.Standard

import Planning.Plans.GamePlans.ModalGamePlan
import Planning.Plans.Protoss.GamePlans.Standard.PvP.{PvPLateGameStandard, PvPOpen2GateRoboObs, PvPOpenDarkTemplar}

class ProtossVsProtossNew extends ModalGamePlan(
  new PvPOpenDarkTemplar,
  new PvPOpen2GateRoboObs,
  new PvPLateGameStandard
)