package Global.Information

import Geometry.Grids.Abstract.GridUnits
import Geometry.Grids.Real._

class Grids {
  val units = new GridUnits
  val enemyGroundStrength = new GridEnemyGroundStrength
  val friendlyGroundStrength = new GridFriendlyGroundStrength
  val enemyVision = new GridEnemyVision
  val enemyDetection = new GridEnemyDetection
  val altitudeBonus = new GridAltitudeBonus
  val buildable = new GridBuildable
  val buildableTerrain = new GridBuildableTerrain
  val walkable = new GridWalkable
  val walkableTerran = new GridWalkableTerrain
  val walkableUnits = new GridWalkableUnits
  val mobility = new GridMobility
  
  def onFrame() {
    units.update()
    enemyGroundStrength.update()
    friendlyGroundStrength.update()
    enemyVision.update()
    enemyDetection.update()
    altitudeBonus.update()
    buildable.update()
    buildableTerrain.update()
    walkable.update()
    walkableTerran.update()
    walkableUnits.update()
    mobility.update()
  }
}
