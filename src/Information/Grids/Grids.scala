package Information.Grids

import Information.Grids.Construction.{GridBuildable, GridBuildableTerrain, GridPsi2Height, GridPsi3Height}
import Information.Grids.Dps._
import Information.Grids.Movement._
import Information.Grids.Vision.{GridAltitudeBonus, GridEnemyDetection}

class Grids {
  
  val altitudeBonus             = new GridAltitudeBonus
  val buildable                 = new GridBuildable
  val buildableTerrain          = new GridBuildableTerrain
  val chokepoints               = new GridChokepoint
  val enemyDetection            = new GridEnemyDetection
  val dpsEnemyAirConcussive     = new GridDpsEnemyAirConcussive
  val dpsEnemyAirExplosive      = new GridDpsEnemyAirExplosive
  val dpsEnemyAirNormal         = new GridDpsEnemyAirNormal
  val dpsEnemyGroundConcussive  = new GridDpsEnemyGroundConcussive
  val dpsEnemyGroundExplosive   = new GridDpsEnemyGroundExplosive
  val dpsEnemyGroundNormal      = new GridDpsEnemyGroundNormal
  val dpsEnemyApproximate       = new GridDpsEnemyApproximate
  val psi2Height                = new GridPsi2Height
  val psi3Height                = new GridPsi3Height
  val mobility                  = new GridMobility
  val units                     = new GridUnits
  val walkable                  = new GridWalkable
  val walkableTerrain           = new GridWalkableTerrain
  val walkableUnits             = new GridWalkableUnits
  val dpsEnemy                  = new GridDpsEnemyToUnit
}
