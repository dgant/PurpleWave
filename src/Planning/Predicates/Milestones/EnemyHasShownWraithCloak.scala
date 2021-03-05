package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Races.Terran

class EnemyHasShownWraithCloak extends Predicate {
  
  var triggered = false
  
  override def apply: Boolean = {
    triggered = triggered || With.units.enemy.exists(u => u.cloaked && u.is(Terran.Wraith))
    triggered
  }
}
