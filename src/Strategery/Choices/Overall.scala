package Strategery.Choices

import Strategery.Strategies._

object Overall extends Choices {
  
  val strategies: Vector[Strategy] = Vector(
    WorkerRush2StartLocations,
    WorkerRush3StartLocations,
    IslandCarriers,
    PvT_Macro,
    PvP_Macro,
    PvZ_FFE,
    PvZ_2Gate
  )
}