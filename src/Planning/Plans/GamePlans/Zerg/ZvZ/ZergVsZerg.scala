package Planning.Plans.GamePlans.Zerg.ZvZ

import Planning.Plans.GamePlans.ModalGameplan
import Planning.Plans.GamePlans.Zerg.ZvE.ZergReactionVsWorkerRush

class ZergVsZerg extends ModalGameplan(
  new ZergReactionVsWorkerRush,
  new ZvZ12Pool,
  new ZvZ9PoolSpeed,
  new ZvZ10HatchLing
)