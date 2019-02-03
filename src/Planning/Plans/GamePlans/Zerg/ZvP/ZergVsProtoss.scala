package Planning.Plans.GamePlans.Zerg.ZvP

import Planning.Plans.GamePlans.ModalGameplan

class ZergVsProtoss extends ModalGameplan(
  new ZvP2HatchMuta,
  new ZvP3Hatch,
  new ZvP6Hatch
)