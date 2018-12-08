
package Information.Grids

import Information.Grids.Combat.GridEnemyRange
import Information.Grids.Construction._
import Information.Grids.Disposable.{GridDisposableBoolean, GridDisposableDouble, GridDisposableInt}
import Information.Grids.Movement._
import Information.Grids.Spells.GridPsionicStorm
import Information.Grids.Vision._

class Grids {

  val altitudeBonus           = new GridAltitudeBonus
  val buildable               = new GridBuildable
  val buildableTerrain        = new GridBuildableTerrain
  val buildableTownHall       = new GridBuildableTownHall
  val creep                   = new GridCreep
  val enemyDetection          = new GridEnemyDetection
  val enemyRange              = new GridEnemyRange
  val enemyVision             = new GridEnemyVision
  val friendlyDetection       = new GridFriendlyDetection
  val friendlyVision          = new GridFriendlyVision
  val harvestingArea          = new GridHarvestingArea
  val mobilityAir             = new GridMobilityAir
  val mobilityBuildings       = new GridMobilityBuildings
  val mobilityGround          = new GridMobilityGround
  val mobilityForceAir        = new GridMobilityForceAir
  val mobilityForceGround     = new GridMobilityForceGround
  val mobilityTerrain         = new GridMobilityTerrain
  val occupancy               = new GridOccupancy
  val psi2Height              = new GridPsi2Height
  val psi3Height              = new GridPsi3Height
  val psionicStorm            = new GridPsionicStorm
  val units                   = new GridUnits
  val walkable                = new GridWalkable
  val walkableTerrain         = new GridWalkableTerrain
  val walkableUnits           = new GridWalkableUnits

  private val _disposableBoolean1 = new GridDisposableBoolean
  private val _disposableBoolean2 = new GridDisposableBoolean
  private val _disposableInt = new GridDisposableInt
  private val _disposableDouble = new GridDisposableDouble

  def disposableBoolean1(): GridDisposableBoolean = {
    _disposableBoolean1.update()
    _disposableBoolean1
  }
  def disposableBoolean2(): GridDisposableBoolean = {
    _disposableBoolean2.update()
    _disposableBoolean2
  }
  def disposableInt(): GridDisposableInt = {
    _disposableInt.update()
    _disposableInt
  }
  def disposableDouble(): GridDisposableDouble = {
    _disposableDouble.update()
    _disposableDouble
  }
}
