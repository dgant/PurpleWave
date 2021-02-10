package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Performance.Cache
import Planning.UnitCounters.{CountUpTo, CountOne}
import Planning.UnitMatchers.{MatchMobileDetector, UnitMatcher}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.{ByOption, CountMap}

class GoalCatchDTRunby extends SquadGoalBasic {

  override def inherentValue: Double = GoalValue.defendBase

  override def toString: String = "Detect DT runbys"
  
  var scout: Option[UnitInfo] = None

  val needed = new Cache(() => With.geography.ourBasesAndSettlements.exists(_.units.forall(u => ! u.isFriendly || ! u.unitClass.isDetector || ! u.complete)))
  override def run(): Unit = {
    if (needed()) {
      unitCounter = CountOne
    } else {
      unitCounter = CountUpTo(0)
    }
    super.run()
  }
  override def destination: Pixel = {
    val dts = With.units.enemy.view.filter(_.is(Protoss.DarkTemplar))
    ByOption.minBy(With.geography.ourBases.map(_.heart.pixelCenter))(heart =>
      ByOption.min(dts.map(_.pixelDistanceCenter(heart)))
        .getOrElse(heart.pixelDistance(With.scouting.mostBaselikeEnemyTile.pixelCenter)))
      .getOrElse(With.geography.home.pixelCenter)
  }

  override def qualityNeeds: CountMap[UnitMatcher] = {
    val output = new CountMap[UnitMatcher]
    output(MatchMobileDetector) = 1
    output
  }

  unitMatcher = MatchMobileDetector
}
