package Strategery.Strategies.Options.Protoss

import Strategery.Strategies.Options.AllRaces.{WorkerRush2StartLocations, WorkerRush3StartLocations}
import Strategery.Strategies.Options.Protoss.Global._
import Strategery.Strategies._

object ProtossChoices {
  
  val options: Iterable[Strategy] = Vector(
    WorkerRush2StartLocations,
    WorkerRush3StartLocations,
    IslandCarriers,
    AllPvT,
    AllPvP,
    AllPvZ)
}