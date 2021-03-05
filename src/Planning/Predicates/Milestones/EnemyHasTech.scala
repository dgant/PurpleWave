package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicate
import ProxyBwapi.Techs.Tech

class EnemyHasTech(tech: Tech) extends Predicate {
  
  override def apply: Boolean = With.enemies.exists(_.hasTech(tech))
  
}
