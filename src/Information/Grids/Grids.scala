package Information.Grids

import Information.Grids.Concrete.Construction.{GridBuildable, GridBuildableTerrain}
import Information.Grids.Concrete.Dps._
import Information.Grids.Concrete.Movement.{GridMobility, GridWalkable, GridWalkableTerrain, GridWalkableUnits}
import Information.Grids.Concrete.Vision.{GridAltitudeBonus, GridEnemyDetection, GridEnemyVision}
import Information.Grids.Concrete.{GridUnits, _}
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
      enemyDetection,
      dpsEnemyAirConcussive,
      dpsEnemyAirExplosive,
      dpsEnemyAirNormal,
      dpsEnemyGroundConcussive,
      dpsEnemyGroundExplosive,
      dpsEnemyGroundNormal,
      enemyVision,
      buildable,
      buildableTerrain,
      walkable,
      walkableTerrain,
      walkableUnits,
      mobility
    ).foreach(_.update())
  }
}
