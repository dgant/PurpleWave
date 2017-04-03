package Information.Grids

import Information.Grids.Construction.{GridBuildable, GridBuildableTerrain, GridPsi2x2and3x2, GridPsi4x3}
import Information.Grids.Dps._
import Information.Grids.Movement.{GridMobility, GridWalkable, GridWalkableTerrain, GridWalkableUnits}
import Information.Grids.Vision.{GridAltitudeBonus, GridEnemyDetection, GridEnemyVision}
import Performance.Caching.Limiter

class Grids {
  
  val frameDelayScale = 1
  
  val altitudeBonus             = new GridAltitudeBonus
  val buildable                 = new GridBuildable
  val buildableTerrain          = new GridBuildableTerrain
  val enemyDetection            = new GridEnemyDetection
  val dpsEnemyAirConcussive     = new GridDpsEnemyAirConcussive
  val dpsEnemyAirExplosive      = new GridDpsEnemyAirExplosive
  val dpsEnemyAirNormal         = new GridDpsEnemyAirNormal
  val dpsEnemyGroundConcussive  = new GridDpsEnemyGroundConcussive
  val dpsEnemyGroundExplosive   = new GridDpsEnemyGroundExplosive
  val dpsEnemyGroundNormal      = new GridDpsEnemyGroundNormal
  val enemyVision               = new GridEnemyVision
  val psi2x2and3x2          = new GridPsi2x2and3x2
  val psi4x3                = new GridPsi4x3
  val mobility                  = new GridMobility
  val units                     = new GridUnits
  val walkable                  = new GridWalkable
  val walkableTerrain           = new GridWalkableTerrain
  val walkableUnits             = new GridWalkableUnits
  val dpsEnemy                  = new GridDpsEnemyToUnit
  
  def onFrame() = limitUpdates.act()
  val limitUpdates = new Limiter(frameDelayScale, () => update)
  def update() {
    List(
      altitudeBonus,
      units,
      dpsEnemyAirConcussive,
      dpsEnemyAirExplosive,
      dpsEnemyAirNormal,
      dpsEnemyGroundConcussive,
      dpsEnemyGroundExplosive,
      dpsEnemyGroundNormal,
      enemyDetection,
      enemyVision,
      psi2x2and3x2,
      psi4x3,
      buildable,
      buildableTerrain,
      walkable,
      walkableTerrain,
      walkableUnits,
      mobility
    ).foreach(_.update())
  }
}
