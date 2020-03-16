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
    var scoutableBases = With.geography.neutralBases
      .filterNot(With.scouting.enemyMain.contains)
      .filterNot(With.scouting.enemyNatural.contains)
      .sortBy(base => - With.scouting.baseIntrigue.getOrElse(base, 0.0))
      .sortBy(_.zone.island)

    val scoutsWanted = PurpleMath.clamp(Math.max(1, With.self.supplyUsed / 80), 0, scoutableBases.size)
    val scouts          = getScouts(matcher, scoutsWanted).toVector.sortBy(_.flying)
    scouts.zipWithIndex.foreach(s => scoutBasesTowardsTownHall(s._1, Seq(scoutableBases(s._2 % scoutableBases.size))))
  }
}