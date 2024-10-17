package Planning.Plans.Gameplans.Zerg.ZvP

import Planning.Plans.Gameplans.All.ModalGameplan
import Planning.Plans.Gameplans.Zerg.ZvE.ZergReactionVsWorkerRush

class ZergVsProtoss extends ModalGameplan(
  new ZergReactionVsWorkerRush,
  new ZvP2HatchMuta,
  new ZvPOpening,
  new ZvPMain,
  new ZvPLateGame
)