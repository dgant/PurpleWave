package Planning.Plans.Scouting

import Lifecycle.With
import Planning.Predicate

class ScoutCleared extends Predicate {
  override def isComplete: Boolean = (
    With.scouting.enemyScouts().isEmpty
    || (
      With.scouting.enemyScouts().forall( ! _.likelyStillThere)
      && With.scouting.basesToLookForEnemyScouts().forall(_.zone.tiles.forall(_.explored))))
}
