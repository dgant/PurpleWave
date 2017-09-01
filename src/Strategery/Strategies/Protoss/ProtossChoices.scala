package Strategery.Strategies.Protoss

import Strategery.Strategies.AllRaces.{WorkerRush2StartLocations, WorkerRush3StartLocations}
import Strategery.Strategies.Protoss.Other.CarriersFromAnIsland
import Strategery.Strategies.Protoss.PvE._
import Strategery.Strategies.Protoss.PvP.AllPvP
import Strategery.Strategies.Protoss.PvR.AllPvR
import Strategery.Strategies.Protoss.PvT.AllPvT
import Strategery.Strategies.Protoss.PvZ.{AllPvZ, PvZ4GateAllIn}
import Strategery.Strategies._

object ProtossChoices {
  
  val overall: Iterable[Strategy] = Vector(
    WorkerRush2StartLocations,
    WorkerRush3StartLocations,
    Proxy2Gate2StartLocations,
    Proxy2Gate3StartLocations,
    ProxyDarkTemplar,
    CarriersFromAnIsland,
    CarriersWithNoDefense,
    DarkArchonsWithNoDefense,
    PvZ4GateAllIn,
    AllPvR,
    AllPvT,
    AllPvP,
    AllPvZ)
}