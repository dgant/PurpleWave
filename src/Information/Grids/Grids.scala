package Information.Grids

import Information.Grids.Construction._
import Information.Grids.Movement._
import Information.Grids.Vision.{GridAltitudeBonus, GridEnemyDetection, GridFriendlyVision}

class Grids {
  
  val altitudeBonus    = new GridAltitudeBonus
  val buildable        = new GridBuildable
  val buildableTerrain = new GridBuildableTerrain
  val chokepoints      = new GridChokepoint
  val creep            = new GridCreep
  val friendlyVision   = new GridFriendlyVision
  val enemyDetection   = new GridEnemyDetection
  val psi2Height       = new GridPsi2Height
  val psi3Height       = new GridPsi3Height
  val mobility         = new GridMobility
  val units            = new GridUnits
  val walkable         = new GridWalkable
  val walkableTerrain  = new GridWalkableTerrain
  val walkableUnits    = new GridWalkableUnits
}
