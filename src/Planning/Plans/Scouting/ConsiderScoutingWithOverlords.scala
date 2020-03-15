package Planning.Plans.Scouting

import Planning.Plans.Compound.{Or, SwitchEnemyRace, Trigger}
import Planning.Predicates.Milestones.EnemiesAtLeast
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

class ConsiderScoutingWithOverlords extends SwitchEnemyRace(
  whenTerran = new Trigger(
    new Or(
      new EnemiesAtLeast(1, Terran.Marine),
      new EnemiesAtLeast(1, Terran.Goliath),
      new EnemiesAtLeast(1, Terran.Wraith),
      new EnemiesAtLeast(1, Terran.Barracks, complete = true)),
    initialBefore = new ScoutWithOverlord),
  whenProtoss = new Trigger(
    new Or(
      new EnemiesAtLeast(1, Protoss.Dragoon),
      new EnemiesAtLeast(1, Protoss.Corsair),
      new EnemiesAtLeast(1, Protoss.Stargate, complete = true),
      new EnemiesAtLeast(1, Protoss.CyberneticsCore, complete = true)),
    initialBefore = new ScoutWithOverlord),
  whenZerg = new Trigger(
    new Or(
      new EnemiesAtLeast(1, Zerg.Mutalisk),
      new EnemiesAtLeast(1, Zerg.Scourge),
      new EnemiesAtLeast(1, Zerg.Hydralisk),
      new EnemiesAtLeast(1, Zerg.Lair),
      new EnemiesAtLeast(1, Zerg.Spire)),
    initialBefore = new ScoutWithOverlord),
  whenRandom = new ScoutWithOverlord
) {
  description.set("Consider Ovie scouting")
}
