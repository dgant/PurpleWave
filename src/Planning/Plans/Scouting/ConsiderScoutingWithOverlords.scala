package Planning.Plans.Scouting

import Planning.Plans.Compound.{SwitchEnemyRace, Trigger}
import Planning.Predicates.Compound.And
import Planning.Predicates.Milestones.EnemiesAtMost
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

class ConsiderScoutingWithOverlords extends SwitchEnemyRace(
  whenTerran = new Trigger(
    new And(
      new EnemiesAtMost(0, Terran.Marine),
      new EnemiesAtMost(0, Terran.Goliath),
      new EnemiesAtMost(0, Terran.Wraith),
      new EnemiesAtMost(0, Terran.Barracks, complete = true)),
    new ScoutWithOverlord),
  whenProtoss = new Trigger(
    new And(
      new EnemiesAtMost(0, Protoss.Dragoon),
      new EnemiesAtMost(0, Protoss.Corsair),
      new EnemiesAtMost(0, Protoss.Stargate, complete = true),
      new EnemiesAtMost(0, Protoss.CyberneticsCore, complete = true)),
    new ScoutWithOverlord),
  whenZerg = new Trigger(
    new And(
      new EnemiesAtMost(0, Zerg.Mutalisk),
      new EnemiesAtMost(0, Zerg.Scourge),
      new EnemiesAtMost(0, Zerg.Hydralisk),
      new EnemiesAtMost(0, Zerg.Lair),
      new EnemiesAtMost(0, Zerg.Spire)),
    new ScoutWithOverlord),
  whenRandom = new ScoutWithOverlord
)
