package Planning.Plans.Scouting

import Lifecycle.With
import ProxyBwapi.Races.Zerg

class ScoutWithOverlord extends AbstractScoutPlan {
  override def onUpdate(): Unit = {
    if (isComplete) return
    if ( ! With.self.isZerg) return
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
        (Seq(main) ++ With.geography.enemyBases).distinct
      } else Seq(main)
      val scouts  = getScouts(Zerg.Overlord, 1)
      scouts.foreach(scoutBasesTowardsTownHall(_, bases))
    } else {
      // Vs Terran:  Scout main with nearest scout
      // Vs Protoss: Scout main's natural with nearest scout
      // Vs Zerg:    Scout main with nearest scout
      // Vs Random:  Scout main with nearest scout

      // Sort by ENTRANCE distance to encourage seeing critical buildings and flying over armies leaving the base
      val candidateBases = With.geography.startBases.filterNot(_.owner.isUs).filterNot(_.scouted)
      val scouts = getScouts(Zerg.Overlord, candidateBases.size).toVector.sortBy(_.frameDiscovered)
      val startBases = candidateBases.sortBy(main => {
          val base = main.natural.filter(! _.scouted && With.enemy.isProtoss).getOrElse(main)
          base.zone.exit
            .map(_.pixelCenter)
            .getOrElse(base.townHallArea.midPixel)
            .pixelDistance(
              scouts
                .headOption
                .map(_.pixelCenter)
                .getOrElse(With.geography.home.pixelCenter))
      })
      val scoutBases = startBases
        .map(b => b.natural.filter(
          With.enemy.isProtoss
          && ! b.scouted
          && _.heart.tileDistanceSquared(With.geography.home) <= b.heart.tileDistanceSquared(With.geography.home)).getOrElse(b))
      scouts.zipWithIndex.foreach(workerAndIndex => scoutBasesTowardsTownHall(workerAndIndex._1, Seq(scoutBases(workerAndIndex._2))))
    }
  }
}
