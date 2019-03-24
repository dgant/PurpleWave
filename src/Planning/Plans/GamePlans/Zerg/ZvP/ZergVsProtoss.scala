package Planning.Plans.GamePlans.Zerg.ZvP

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush

class ZergVsProtoss extends ModalGameplan(
  new ZergReactionVsWorkerRush,
  new ZvP2HatchMuta,
  new ZvP3Hatch,
  new ZvP6Hatch
)