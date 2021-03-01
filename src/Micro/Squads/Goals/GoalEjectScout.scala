package Micro.Squads.Goals

import Lifecycle.With
import Micro.Agency.Intention
import Performance.Cache
import Utilities.ByOption

class GoalEjectScout extends SquadGoal {

  val targetScout = new Cache(() => ByOption.minBy(With.scouting.enemyScouts())(_.frameDiscovered))
  private val tilesToConsider = new Cache(() => With.scouting.basesToLookForEnemyScouts().view.flatMap(_.zone.tiles))
  private val destination = new Cache(() => targetScout()
    .map(_.pixel)
    .getOrElse(ByOption.minBy(tilesToConsider())(With.grids.lastSeen.get).getOrElse(With.geography.home).pixelCenter))
  
  override def run() {
    squad.units.foreach(ejector => {
      ejector.agent.intend(this, new Intention {
        toScoutTiles = if (targetScout().exists(_.likelyStillThere)) Seq.empty else tilesToConsider()
        toTravel = Some(destination())
        toAttack = if (ejector.matchups.targets.forall(targetScout().contains)) targetScout() else None
      })
    })
  }
}
