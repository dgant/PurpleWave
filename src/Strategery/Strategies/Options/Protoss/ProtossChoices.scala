package Strategery.Strategies.Options.Protoss

import Strategery.Strategies.Options.AllRaces.{WorkerRush2StartLocations, WorkerRush3StartLocations}
import Strategery.Strategies.Options.Protoss.Global._
import Strategery.Strategies._

object ProtossChoices {
  
  val overall: Iterable[Strategy] = Vector(
    WorkerRush2StartLocations,
    WorkerRush3StartLocations,
    Proxy2Gate2StartLocations,
    Proxy2Gate3StartLocations,
    IslandCarriers,
    AllPvR,
    AllPvT,
    AllPvP,
    AllPvZ)
}