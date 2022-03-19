package Planning.UnitPreferences

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.Seconds
import bwapi.Race

case class PreferTrainerFor(traineeClass: UnitClass) extends UnitPreference {
  private val safetyFramesMax = Seconds(10)()
  override def apply(trainer: FriendlyUnitInfo): Double = {
    if (trainer.unitClass.race == Race.Zerg) {
      if (traineeClass.isWorker) {
        return PreferAll(PreferHatcheryWithThreeLarva, PreferBaseWithFewerWorkers)(trainer)
      } else if (traineeClass.whatBuilds._1 == Zerg.Larva) {
        return PreferAll(PreferHatcheryWithThreeLarva, PreferBaseWithMoreWorkers)(trainer)
      }
    }

    // These factors should produce unambiguous ordering based on factor importance;
    // either they should range on [0, 1] or have discrete { -1, 0, 1 } values

    // Strongly prefer a trainer already making the class to avoid accidentally picking up and training another unit.
    val already   = trainer.trainee.map(t => if (traineeClass(t)) -1.0 else 1.0).getOrElse(0.0)
    val noAddon   = Maff.fromBoolean(trainer.addon.isEmpty)
    val readiness = trainer.remainingOccupationFrames.toDouble / Math.max(trainer.trainee.map(_.unitClass.buildFrames).getOrElse(0), traineeClass.buildFrames)
    val workers   = if (traineeClass.isWorker) Maff.clamp(trainer.base.map(_.saturation()).getOrElse(0.0), 0.0, 1.0) else 0.0
    val health    = trainer.totalHealth / trainer.unitClass.maxTotalHealth.toDouble
    val safety    = Maff.clamp(trainer.matchups.framesOfSafety, 0, safetyFramesMax) / safetyFramesMax.toDouble
    val distance  = 1.0 - Maff.clamp(trainer.tile.groundTiles(With.scouting.mostBaselikeEnemyTile) / (With.mapTileWidth + With.mapTileHeight), 0.0, 1.0)
    val score = (
        1000000000000.0 * already
      + 10000000000.0   * noAddon
      + 100000000.0     * workers
      + 1000000.0       * readiness
      + 10000.0         * health
      + 100.0           * safety
      + 1.0             * distance
    )
    score
  }
}
