package Planning.Plans.GamePlans.Protoss.PvZ

import Planning.Plans.GamePlans.All.ModalGameplan

class ProtossVsZerg extends ModalGameplan(
  new PvZFE,
  new PvZ1BaseReactive,
  new PvZ2022
)