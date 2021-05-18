
package Information.Grids

import Information.Grids.Combat.{GridEnemyRangeAir, GridEnemyRangeAirGround, GridEnemyRangeGround}
import Information.Grids.Construction._
import Information.Grids.Versioned.{GridVersionedBoolean, GridVersionedDouble, GridVersionedInt}
import Information.Grids.Movement._
import Information.Grids.Spells.GridPsionicStorm
import Information.Grids.Vision._

class Grids {
  val buildable                   = new GridBuildable
  val buildableTerrain            = new GridBuildableTerrain
  val buildableTownHall           = new GridBuildableTownHall
  val lastSeen                    = new GridLastSeen
  val psi2Height                  = new GridPsi2Height
  val psi3Height                  = new GridPsi3Height
  val psionicStorm                = new GridPsionicStorm
  val scoutingPathsBases          = new GridScoutingPathsBases
  val scoutingPathsStartLocations = new GridScoutingPathsStartLocations
  val walkable                    = new GridWalkable
  val walkableTerrain             = new GridWalkableTerrain

  // Flood-filly grids updated on unit movement
  val units                       = new GridUnits
  val enemyDetection              = new GridEnemyDetection
  val enemyRangeAir               = new GridEnemyRangeAir
  val enemyRangeGround            = new GridEnemyRangeGround
  val enemyRangeAirGround         = new GridEnemyRangeAirGround
  val enemyVision                 = new GridEnemyVision
  val friendlyDetection           = new GridFriendlyDetection
  val unwalkableUnits             = new GridUnwalkableUnits

  private val _disposableBoolean  = new GridVersionedBoolean
  private val _disposableInt      = new GridVersionedInt
  private val _disposableDouble   = new GridVersionedDouble

  def disposableBoolean(): GridVersionedBoolean = {
    _disposableBoolean.update()
    _disposableBoolean
  }
  def disposableInt(): GridVersionedInt = {
    _disposableInt.update()
    _disposableInt
  }
  def disposableDouble(): GridVersionedDouble = {
    _disposableDouble.update()
    _disposableDouble
  }
}
