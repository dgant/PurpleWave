package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsZerg extends ModalGameplan(
  // Openings
  new PvZ1BaseForgeTech,
  new PvZ10Gate,
  new PvZ2Gate910,
  new PvZ2Gate1012,
  new PvZFFE,

  // Midgames
  new PvZBisu,
  new PvZNeoBisu,
  new PvZ5GateGoon,
  new PvZCorsairReaver,

  // Late game
  new PvZLateGameReaver,
  new PvZLateGameTemplar,
)