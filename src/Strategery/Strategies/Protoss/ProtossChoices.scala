package Strategery.Strategies.Protoss

import Strategery.Strategies.AllRaces.{WorkerRush2StartLocations, WorkerRush3StartLocations}
import Strategery.Strategies.Protoss.Global._
import Strategery.Strategies._

object ProtossChoices {
  
  val overall: Iterable[Strategy] = Vector(
    WorkerRush2StartLocations,
    WorkerRush3StartLocations,
    Proxy2Gate2StartLocations,
    Proxy2Gate3StartLocations,
    ProxyDarkTemplar,
    IslandCarriers,
    AllPvR,
    AllPvT,
    AllPvP,
    AllPvZ)
}