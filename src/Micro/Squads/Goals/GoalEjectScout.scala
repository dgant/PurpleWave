package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import Micro.Agency.Intention
import Planning.Plans.Scouting.ScoutTracking
import Utilities.ByOption

class GoalEjectScout extends SquadGoalBasic {

  override def inherentValue: Double = GoalValue.defendBase
  
  override def toString: String = "Eject scouts"
  
  override def destination: Pixel = targetScout
    .map(_.pixel)
    .getOrElse(
      ByOption.minBy(tilesToConsider)(With.grids.lastSeen.get).getOrElse(With.geography.home).pixelCenter)

  private def tilesToConsider: Seq[Tile] = ScoutTracking.basesToConsider.view.flatMap(_.zone.tiles)

  private def targetScout = ByOption.maxBy(squad.enemies.filter(_.likelyStillThere))(_.lastSeen)
  
  override def run() {
    squad.units.foreach(ejector => {
      ejector.agent.intend(squad.client, new Intention {
        toScoutTiles = if (targetScout.exists(_.likelyStillThere)) Seq.empty else tilesToConsider
        toTravel = Some(destination)
        toAttack = if (ejector.matchups.targets.forall(targetScout.contains)) targetScout else None
      })
    })
  }
}
