package Information.Grids

import Information.Grids.Construction.{GridBuildable, GridBuildableTerrain, GridPsi2Height, GridPsi3Height}
import Information.Grids.Dpf._
import Information.Grids.Movement._
import Information.Grids.Vision.{GridAltitudeBonus, GridEnemyDetection}

class Grids {
  
  val altitudeBonus             = new GridAltitudeBonus
  val buildable                 = new GridBuildable
  val buildableTerrain          = new GridBuildableTerrain
  val chokepoints               = new GridChokepoint
  val enemyDetection            = new GridEnemyDetection
  val dpfEnemyAirConcussive     = new GridDpfEnemyAirConcussive
  val dpfEnemyAirExplosive      = new GridDpfEnemyAirExplosive
  val dpfEnemyAirNormal         = new GridDpfEnemyAirNormal
  val dpfEnemyGroundConcussive  = new GridDpfEnemyGroundConcussive
  val dpfEnemyGroundExplosive   = new GridDpfEnemyGroundExplosive
  val dpfEnemyGroundNormal      = new GridDpfEnemyGroundNormal
  val dpfEnemyApproximate       = new GridDpfEnemyApproximate
  val psi2Height                = new GridPsi2Height
  val psi3Height                = new GridPsi3Height
  val mobility                  = new GridMobility
  val units                     = new GridUnits
  val walkable                  = new GridWalkable
  val walkableTerrain           = new GridWalkableTerrain
  val walkableUnits             = new GridWalkableUnits
  val dpfEnemy                  = new GridDpfEnemyToUnit
}
