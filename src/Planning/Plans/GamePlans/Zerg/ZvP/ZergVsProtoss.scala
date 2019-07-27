package Planning.Plans.GamePlans.Zerg.ZvP

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush
import Planning.Plans.GamePlans.Zerg.ZvPNew.{ZvPLateGame, ZvPMain, ZvPOpening}

class ZergVsProtoss extends ModalGameplan(
  new ZergReactionVsWorkerRush,
  new ZvP2HatchMuta,
  new ZvP3Hatch,
  new ZvP6Hatch,

  new ZvPOpening,
  new ZvPMain,
  new ZvPLateGame
)