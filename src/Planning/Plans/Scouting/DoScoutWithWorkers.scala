package Planning.Plans.Scouting

import Lifecycle.With
import Planning.UnitMatchers.MatchWorkers
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Strategery.Strategies.Zerg.{ZvE4Pool, ZvT1HatchHydra}
import Utilities.ByOption
import bwapi.Race

class DoScoutWithWorkers(maxScouts: Int = 1) extends DoScout {
  var lastScouts: Seq[FriendlyUnitInfo] = Seq.empty
  private var scoutDied: Boolean = false
  def update(): Unit = {
    scoutDied ||= lastScouts.exists( ! _.alive)
    if (scoutDied) return
    if (With.blackboard.maximumScouts() < 1) return
    if (With.units.countOurs(MatchWorkers) < 3) return
    if (With.scouting.enemyMain.exists(main => ByOption.minBy(With.units.ours)(_.framesToTravelTo(main.heart.pixelCenter)).exists( ! _.unitClass.isWorker))) return
    // With 4Pool use the scout to help harass/distract
    if ( ! ZvE4Pool.registerActive && ! ZvT1HatchHydra.registerActive && With.geography.enemyBases.exists(_.units.exists(u => u.unitClass.isStaticDefense && u.complete))) return
    if (With.scouting.firstEnemyMain.isDefined) {
      // Vs Non-random: Scout least-claimed known main + its natural
      // Vs Random:     Scout least-claimed known main (to determine race)
      val main    = With.scouting.firstEnemyMain.get
      val scouts  = getScouts(MatchWorkers, 1).toSeq
      val bases   = if (With.enemy.raceCurrent == Race.Unknown) Seq(main) else Seq(main) ++ main.natural
      scouts.foreach(scoutBasesTowardsTownHall(_, bases))
      lastScouts = scouts
    } else {
      // Scout least-claimed known main
      val bases   = With.geography.startBases.filterNot(_.scouted).sortBy(_.townHallTile.groundPixels(With.geography.home)).sortBy(With.scouting.baseScouts)
      val scouts  = getScouts(MatchWorkers, Math.min(maxScouts, bases.size)).toSeq
      scouts.zipWithIndex.foreach(workerAndIndex => scoutBasesTowardsTownHall(workerAndIndex._1, Seq(bases(workerAndIndex._2))))
      lastScouts = scouts
    }
  }
}