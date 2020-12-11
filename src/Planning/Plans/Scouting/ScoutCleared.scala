package Planning.Plans.Scouting

import Lifecycle.With
import Planning.Predicate

class ScoutCleared extends Predicate {
  override def isComplete: Boolean = (
    ScoutTracking.enemyScouts.isEmpty
    || (
      ScoutTracking.enemyScouts.forall( ! _.likelyStillThere)
      && ScoutTracking.basesToConsider.forall(_.zone.tiles.forall(With.grids.friendlyVision.get(_) > 0))))
}
