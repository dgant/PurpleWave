package Planning.Plans.Scouting

import Planning.Plans.Compound.{If, SwitchOurRace}
import Planning.Predicates.Strategy.StartPositionsAtLeast
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

class ConsiderScoutingWithWorker extends SwitchOurRace(
  whenTerran = new If(
    new StartPositionsAtLeast(3),
    new ScoutOn(Terran.SupplyDepot),
    new ScoutOn(Terran.Barracks)),
  whenProtoss = new If(
    new StartPositionsAtLeast(3),
    new ScoutOn(Protoss.Pylon),
    new ScoutOn(Protoss.Gateway)),
  whenZerg = new If(
    new StartPositionsAtLeast(3),
    new ScoutOn(Zerg.Overlord, quantity = 2),
    new ScoutAt(12)))