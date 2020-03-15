package Planning.Plans.Scouting

import Lifecycle.With
import Mathematics.PurpleMath
import Planning.UnitMatchers._
import ProxyBwapi.Races.{Protoss, Terran}

class ScoutExpansions(matcher: UnitMatcher = UnitMatchAnd(
  UnitMatchRecruitableForCombat,
  UnitMatchNot(_.topSpeed < Protoss.Zealot.topSpeed),
  UnitMatchNot(UnitMatchWorkers),
  UnitMatchNot(Terran.Battlecruiser),
  UnitMatchNot(Terran.Medic),
  UnitMatchNot(Terran.Dropship),
  UnitMatchNot(Terran.Valkyrie),
  UnitMatchNot(Protoss.Shuttle),
  UnitMatchNot(Protoss.Carrier))) extends AbstractScoutPlan {

  override protected def onUpdate(): Unit = {
    val scoutableBases  = With.geography.neutralBases.sortBy(base => With.scouting.baseIntrigue.getOrElse(base, 0.0))
    val scoutsWanted    = PurpleMath.clamp(With.self.supplyUsed / 80, 1, scoutableBases.size)
    val scouts          = getScouts(matcher, 1)
    scouts.zipWithIndex.foreach(s => scoutBasesTowardsTownHall(s._1, Seq(scoutableBases(s._2))))
  }
}