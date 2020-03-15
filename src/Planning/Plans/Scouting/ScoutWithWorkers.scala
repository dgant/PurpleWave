package Planning.Plans.Scouting

import Lifecycle.With
import Planning.UnitMatchers.UnitMatchWorkers
import Strategery.Strategies.Zerg.{ZvE4Pool, ZvT1HatchHydra}
import Utilities.ByOption
import bwapi.Race

class ScoutWithWorkers(maxScouts: Int = 1) extends AbstractScoutPlan {
  override def isComplete: Boolean = {
    if (With.blackboard.lastScoutDeath > 0) return true
    if (With.units.countOurs(UnitMatchWorkers) < 3) return true
    if (With.scouting.enemyMain.exists(main => ByOption.minBy(With.units.ours)(_.framesToTravelTo(main.heart.pixelCenter)).exists( ! _.unitClass.isWorker))) return true
    // With 4Pool use the scout to help harass/distract
    if ( ! ZvE4Pool.active && ! ZvT1HatchHydra.active && With.geography.enemyBases.exists(_.units.exists(u => u.unitClass.isStaticDefense && u.complete))) return true
    false
  }
  override def onUpdate(): Unit = {
    if (With.scouting.firstEnemyMain.isDefined) {
      // Vs Non-random: Scout least-claimed known main + its natural
      // Vs Random:     Scout least-claimed known main (to determine race)
      val main    = With.scouting.firstEnemyMain.get
      val scouts  = getScouts(UnitMatchWorkers, 1)
      val bases   = if (With.enemy.raceCurrent == Race.Unknown) Seq(main) else Seq(main) ++ main.natural
      scouts.foreach(scoutBasesTowardsTownHall(_, bases))
    } else {
      // Scout least-claimed known main
      val bases   = With.geography.startBases.filterNot(_.scouted).sortBy(_.townHallTile.groundPixels(With.geography.home)).sortBy(With.scouting.baseScouts)
      val scouts  = getScouts(UnitMatchWorkers, Math.min(maxScouts, bases.size))
      scouts.zipWithIndex.foreach(workerAndIndex => scoutBasesTowardsTownHall(workerAndIndex._1, Seq(bases(workerAndIndex._2))))
    }
  }
}