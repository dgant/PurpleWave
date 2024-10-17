package Planning.Plans.Gameplans.Zerg.ZvZ

import Planning.Plans.Gameplans.All.ModalGameplan
import Planning.Plans.Gameplans.Zerg.ZvE.ZergReactionVsWorkerRush

class ZergVsZerg extends ModalGameplan(
  new ZergReactionVsWorkerRush,
  new ZvZ12Pool,
  new ZvZ9PoolSpeed,
  new ZvZ10HatchLing
)