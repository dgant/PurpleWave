package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsZerg extends ModalGameplan(
  // Openings
  new PvZ4Gate99,
  new PvZ4Gate,
  new PvZFFE,
  // Midgames
  new PvZSpeedlotTemplar,
  new PvZ8Gate,
  new PvZ4Gate2Archon,
  // Late game
  new PvZLateGame
)