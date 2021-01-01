package Planning.Plans.Scouting

import Planning.Predicate

class ScoutCleared extends Predicate {
  override def isComplete: Boolean = (
    ScoutTracking.enemyScouts.isEmpty
    || (
      ScoutTracking.enemyScouts.forall( ! _.likelyStillThere)
      && ScoutTracking.basesToConsider.forall(_.zone.tiles.forall(_.explored))))
}
