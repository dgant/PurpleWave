package Planning.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Races.Zerg

class EnemyLurkers extends Predicate {
  
  override def apply: Boolean = {
    With.units.existsEnemy(Zerg.Lurker) ||
    With.units.existsEnemy(Zerg.LurkerEgg) ||
    (
      (
        With.units.existsEnemy(Zerg.Hydralisk) ||
        With.units.existsEnemy(Zerg.HydraliskDen)
      )
      && With.units.existsEnemy(Zerg.Lair)
    )
  }
}
