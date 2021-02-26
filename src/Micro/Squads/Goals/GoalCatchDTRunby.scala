package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import ProxyBwapi.Races.Protoss
import Utilities.ByOption

class GoalCatchDTRunby extends SquadGoal {

  override def run(): Unit = {
    squad.units.foreach(_.agent.intend(this, new Intention { toTravel = Some(destination)}))
  }

  private def destination: Pixel = {
    val dts = With.units.enemy.view.filter(_.is(Protoss.DarkTemplar))
    ByOption.minBy(With.geography.ourBases.map(_.heart.pixelCenter))(heart =>
      ByOption.min(dts.map(_.pixelDistanceCenter(heart)))
        .getOrElse(heart.pixelDistance(With.scouting.mostBaselikeEnemyTile.pixelCenter)))
      .getOrElse(With.geography.home.pixelCenter)
  }
}
