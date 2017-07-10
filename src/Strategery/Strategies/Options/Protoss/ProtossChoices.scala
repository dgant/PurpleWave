package Strategery.Strategies.Options.Protoss

import Strategery.Strategies.Options.AllRaces.{Proxy2Gate2StartLocations, WorkerRush2StartLocations, WorkerRush3StartLocations}
import Strategery.Strategies.Options.Protoss.Global._
import Strategery.Strategies._

object ProtossChoices {
  
  val options: Iterable[Strategy] = Vector(
    WorkerRush2StartLocations,
    WorkerRush3StartLocations,
    Proxy2Gate2StartLocations,
    IslandCarriers,
    AllPvR,
    AllPvT,
    AllPvP,
    AllPvZ)
}