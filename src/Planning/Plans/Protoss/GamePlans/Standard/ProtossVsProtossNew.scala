package Planning.Plans.Protoss.GamePlans.Standard

import Planning.Plans.GamePlans.ModalGamePlan
import Planning.Plans.Protoss.GamePlans.Standard.PvP.{PvPLateGameStandard, PvPOpenDarkTemplar}

class ProtossVsProtossNew extends ModalGamePlan(
  new PvPOpenDarkTemplar,
  new PvPLateGameStandard
)