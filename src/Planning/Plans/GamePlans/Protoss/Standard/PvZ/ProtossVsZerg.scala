package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsZerg extends ModalGameplan(
  // Openings
  new PvZ1BaseForgeTech,
  new PvZFFE,

  // Midgames
  new PvZBisu,
  new PvZ5GateGoon,
  new PvZCorsairReaver,

  // Late game
  new PvZLateGameReaver,
  new PvZLateGameTemplar,
)