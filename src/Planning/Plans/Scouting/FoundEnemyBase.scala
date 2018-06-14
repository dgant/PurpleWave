package Planning.Plans.Scouting

import Lifecycle.With
import Planning.Predicate

class FoundEnemyBase extends Predicate {
  
  override def isComplete: Boolean = With.geography.enemyBases.nonEmpty ||
    //TODO -- once we understand implicit base positions, remove this
    With.units.enemy.exists(e => e.unitClass.isBuilding && e.possiblyStillThere)
}
