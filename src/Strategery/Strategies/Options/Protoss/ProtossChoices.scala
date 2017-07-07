package Strategery.Strategies.Options.Protoss

import Strategery.Strategies.Options.AllRaces.{WorkerRush2StartLocations, WorkerRush3StartLocations}
import Strategery.Strategies.Options.Protoss.Choices._
import Strategery.Strategies._

object ProtossChoices extends StrategyChoice {
  
  val options: Vector[Strategy] = Vector(
    WorkerRush2StartLocations,
    WorkerRush3StartLocations,
    IslandCarriers,
    PvT_Macro,
    PvP_Macro,
    PvZ_FFE,
    PvZ_2Gate
  )
}