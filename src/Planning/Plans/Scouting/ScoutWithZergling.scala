package Planning.Plans.Scouting

import Lifecycle.With
import ProxyBwapi.Races.Zerg

class ScoutWithZergling extends AbstractScoutPlan {

  override def isComplete: Boolean = With.scouting.firstEnemyMain.isDefined

  override def onUpdate(): Unit = {
    val scouts = getScouts(Zerg.Zergling, 1)
    scouts.foreach(scoutBasesTowardsTownHall(_, Seq(With.scouting.baseIntrigue.maxBy(_._2)._1)))
  }
}
