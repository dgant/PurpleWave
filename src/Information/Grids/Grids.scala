
package Information.Grids

import Information.Grids.Construction._
import Information.Grids.Movement._
import Information.Grids.Spells.GridPsionicStorm
import Information.Grids.Vision._

class Grids {
  
  val altitudeBonus           = new GridAltitudeBonus
  val buildable               = new GridBuildable
  val buildableTerrain        = new GridBuildableTerrain
  val chokepoints             = new GridChokepoint
  val creep                   = new GridCreep
  val friendlyVision          = new GridFriendlyVision
  val enemyVision             = new GridEnemyVision
  val enemyDetection          = new GridEnemyDetection
  val friendlyDetection       = new GridFriendlyDetection
  val mobilityTerrain         = new GridMobilityTerrain
  val mobilityBuildings       = new GridMobilityBuildings
  val mobilityAir             = new GridMobilityAir
  val mobilityGround          = new GridMobilityGround
  val mobilityForceAir        = new GridMobilityForceAir
  val mobilityForceGround     = new GridMobilityForceGround
  val psi2Height              = new GridPsi2Height
  val psi3Height              = new GridPsi3Height
  val psionicStorm            = new GridPsionicStorm
  val units                   = new GridUnits
  val walkable                = new GridWalkable
  val walkableTerrain         = new GridWalkableTerrain
  val walkableUnits           = new GridWalkableUnits
}
