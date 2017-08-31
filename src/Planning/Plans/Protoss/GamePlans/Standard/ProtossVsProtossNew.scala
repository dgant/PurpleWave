package Planning.Plans.Protoss.GamePlans.Standard

import Lifecycle.With
import Planning.Plan
import Planning.Plans.GamePlans.ModalGamePlan
import Planning.Plans.Protoss.GamePlans.Standard.PvP.{PvPLateGameStandard, PvPOpen2GateRoboObs, PvPOpenDarkTemplar}

class ProtossVsProtossNew extends ModalGamePlan(
  new Plan { override def onUpdate() {  With.blackboard.gasBankSoftLimit = 450 }},
  new PvPOpenDarkTemplar,
  new PvPOpen2GateRoboObs,
  new PvPLateGameStandard
)