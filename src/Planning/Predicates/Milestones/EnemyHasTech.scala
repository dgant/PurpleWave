package Planning.Predicates.Milestones

import Lifecycle.With
import Planning.Predicates.Predicate
import ProxyBwapi.Techs.Tech

case class EnemyHasTech(tech: Tech) extends Predicate {
  override def apply: Boolean = With.enemies.exists(_.hasTech(tech))
}
