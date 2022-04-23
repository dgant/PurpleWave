package Utilities.UnitPreferences

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.{Terran, Zerg}
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
    //
    val noAddon   = Maff.fromBoolean(trainer.addon.isEmpty)
    val readiness = trainer.remainingOccupationFrames.toDouble / Math.max(trainer.trainee.map(_.unitClass.buildFrames).getOrElse(0), traineeClass.buildFrames)
    val workers   = if (traineeClass.isWorker) Maff.clamp(trainer.base.map(_.saturation()).getOrElse(0.0), 0.0, 1.0) else 0.0
    val defend    = if (traineeClass.attacksGround && ! traineeClass.isWorker) Maff.clamp(trainer.base.map(_.enemyCombatValue).getOrElse(0d) / Terran.Vulture.subjectiveValue, 0d, 1d) else 0d
    val health    = trainer.totalHealth / trainer.unitClass.maxTotalHealth.toDouble
    val distance  = 1.0 - Maff.clamp(trainer.tile.groundTiles(With.scouting.enemyHome) / (With.mapTileWidth + With.mapTileHeight), 0.0, 1.0)
    val score = (
      + 100000.0  * noAddon
      + 10000.0   * readiness
      + 1000.0    * workers
      + 100.0     * defend
      + 10.0      * health
      + 1.0       * distance
    )
    score
  }
}
