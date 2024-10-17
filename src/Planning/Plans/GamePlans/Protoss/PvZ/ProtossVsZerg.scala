package Planning.Plans.Gameplans.Protoss.PvZ

import Planning.Plans.Gameplans.All.ModalGameplan

class ProtossVsZerg extends ModalGameplan(
  new PvZFE,
  new PvZ1BaseReactive,
  new PvZ2022
)