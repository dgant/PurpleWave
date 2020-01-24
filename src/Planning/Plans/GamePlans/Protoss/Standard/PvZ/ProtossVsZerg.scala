package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Planning.Plans.GamePlans.ModalGameplan

class ProtossVsZerg extends ModalGameplan(
  // Openings
  new PvZ1BaseForgeTech,
  new PvZ4Gate910,
  new PvZ4Gate,
  new PvZFFE,

  // Midgames
  new PvZ4Gate2Archon,
  new PvZBisu,
  new PvZNeoBisu,
  new PvZNeoNeoBisu,
  new PvZ5GateGoon,
  new PvZCorsairReaver,

  // Late game
  new PvZLateGameReaver,
  new PvZLateGameTemplar,
  new PvZLateGameCarrier,
)