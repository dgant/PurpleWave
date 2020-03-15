package Planning.Plans.Scouting

import Lifecycle.With
import ProxyBwapi.Races.Zerg

class ScoutWithOverlord extends AbstractScoutPlan {
  override def onUpdate(): Unit = {
    if ( ! With.self.isZerg) return
    if (With.blackboard.lastScoutDeath > 0) return

    if (With.scouting.firstEnemyMain.isDefined) {
      // Vs Terran:  Scout least-claimed known main + its natural
      // Vs Protoss: Scout least-claimed known main + its natural
      // Vs Zerg:    Scout least-claimed known bases
      // Vs Random:  Scout least-claimed known main (to determine race)
      // TODO LATER: Then scout bases nearest opponent
      val main    = With.scouting.firstEnemyMain.get
      val bases   = if (With.enemy.isTerran || With.enemy.isProtoss) {
        Seq(main) ++ main.natural
      } else if (With.enemy.isZerg) {
        With.geography.enemyBases
      } else Seq(main)
      val scouts  = getScouts(Zerg.Overlord, 1)
      scouts.foreach(scoutBasesTowardsTownHall(_, bases))
    } else {
      // Vs Terran:  Scout least-claimed main nearest scout
      // Vs Protoss: Scout least-claimed main's natural nearest scout
      // Vs Zerg:    Scout least-claimed main nearest scout
      // Vs Random:  Scout least-claimed main nearest scout
      val bases   = With.geography.startBases.filterNot(_.scouted).sortBy(_.townHallTile.groundPixels(With.geography.home))
      val scouts  = getScouts(Zerg.Overlord, bases.size)
      scouts.zipWithIndex.foreach(workerAndIndex => scoutBasesTowardsTownHall(workerAndIndex._1, Seq(bases(workerAndIndex._2))))
    }
  }
}
