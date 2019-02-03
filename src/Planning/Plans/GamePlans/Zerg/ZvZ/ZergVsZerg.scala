package Planning.Plans.GamePlans.Zerg.ZvZ

import Planning.Plans.GamePlans.ModalGameplan

class ZergVsZerg extends ModalGameplan(
  new ZvZ12Pool,
  new ZvZ5PoolSunkens,
  new ZvZ9PoolSpeed,
  new ZvZ10HatchLing
)