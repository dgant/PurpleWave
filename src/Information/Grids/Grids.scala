package Information.Grids

import Information.Grids.Concrete.{GridUnits, _}

class Grids {
  val altitudeBonus = new GridAltitudeBonus
  val buildable = new GridBuildable
  val buildableTerrain = new GridBuildableTerrain
  val enemyDetection = new GridEnemyDetection
  val enemyGroundStrength = new GridEnemyGroundStrength
  val enemyVision = new GridEnemyVision
  val friendlyGroundStrength = new GridFriendlyGroundStrength
  val mobility = new GridMobility
  val units = new GridUnits
  val walkable = new GridWalkable
  val walkableTerran = new GridWalkableTerrain
  val walkableUnits = new GridWalkableUnits
  
  def onFrame() {
    altitudeBonus.update()
    units.update()
    enemyDetection.update()
    enemyGroundStrength.update()
    enemyVision.update()
    friendlyGroundStrength.update()
    buildable.update()
    buildableTerrain.update()
    walkable.update()
    walkableTerran.update()
    walkableUnits.update()
    mobility.update()
  }
}
