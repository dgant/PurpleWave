package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

class GoalCatchDTRunby extends GoalBasic {
  
  override def toString: String = "Detect DT runbys"
  
  var scout: Option[UnitInfo] = None
  
  override def destination: Pixel = {
    val dts = With.units.enemy.filter(_.is(Protoss.DarkTemplar))
    ByOption.minBy(With.geography.ourBases.map(_.heart.pixelCenter))(heart =>
      ByOption.min(dts.map(_.pixelDistanceCenter(heart)))
        .getOrElse(heart.pixelDistance(With.intelligence.mostBaselikeEnemyTile.pixelCenter)))
      .getOrElse(With.geography.home.pixelCenter)
  }

  override def acceptsHelp: Boolean = squad.units.isEmpty

  override protected def offerCritical(candidates: Iterable[FriendlyUnitInfo]): Unit = {}
  override protected def offerImportant(candidates: Iterable[FriendlyUnitInfo]): Unit = {
    if (acceptsHelp) {
      ByOption.minBy(candidates)(_.pixelDistanceEdge(destination)).foreach(addCandidate)
    }
  }
  override protected def offerUseful(candidates: Iterable[FriendlyUnitInfo]): Unit =  {}
  override protected def offerUseless(candidates: Iterable[FriendlyUnitInfo]): Unit = {}
}
