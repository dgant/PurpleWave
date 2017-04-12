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
  val psi2x2and3x2              = new GridPsi2x2and3x2
  val psi4x3                    = new GridPsi4x3
  val mobility                  = new GridMobility
  val units                     = new GridUnits
  val walkable                  = new GridWalkable
  val walkableTerrain           = new GridWalkableTerrain
  val walkableUnits             = new GridWalkableUnits
  val dpsEnemy                  = new GridDpsEnemyToUnit
  
  // Updating grids is the spikiest thing we do, performance-wise
  // So we split the grid updates into batches so no one frame gets too long.
  private val updateBatches = Array(
    Vector(
      psi2x2and3x2,
      psi4x3,
      walkable,
      walkableTerrain,
      walkableUnits,
      buildable,
      buildableTerrain
    ),
    Vector(
      units
    ),
    Vector(
      altitudeBonus,
      enemyDetection,
      enemyVision
    ),
    Vector(
      mobility //The most expensive by far
    ),
    Vector(
      dpsEnemyAirConcussive,
      dpsEnemyAirExplosive,
      dpsEnemyAirNormal,
      dpsEnemyGroundConcussive,
      dpsEnemyGroundExplosive,
      dpsEnemyGroundNormal
    )
  )
  
  private var lastBatchUpdated = 0
  def onFrame() = limitUpdates.act()
  val limitUpdates = new Limiter(1, () => update)
  
  def update() {
    lastBatchUpdated = (lastBatchUpdated + 1) % updateBatches.size
    updateBatches(lastBatchUpdated).foreach(_.update())
  }
}
