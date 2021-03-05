package Planning.Predicates.Reactive

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Races.Zerg

class EnemyHydralisks extends Predicate {
  
  override def apply: Boolean = {
    With.units.existsEnemy(Zerg.Hydralisk, Zerg.HydraliskDen)
  }
}
