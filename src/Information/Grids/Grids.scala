package Information.Grids

import Information.Grids.Concrete.{GridUnits, _}
import Performance.Caching.Limiter

class Grids {
  val altitudeBonus = new GridAltitudeBonus
  val buildable = new GridBuildable
  val buildableTerrain = new GridBuildableTerrain
  val enemyDetection = new GridEnemyDetection
  val enemyStrength = new GridEnemyStrength
  val enemyVision = new GridEnemyVision
  val friendlyVision = new GridFriendlyVision
  val friendlyStrength = new GridFriendlyStrength
  val mobility = new GridMobility
  val units = new GridUnits
  val walkable = new GridWalkable
  val walkableTerran = new GridWalkableTerrain
  val walkableUnits = new GridWalkableUnits
  
  val updateFrequency = 1
  
  def onFrame() = limitUpdates.act()
  val limitUpdates = new Limiter(updateFrequency, () => update)
  def update() {
    List(
      altitudeBonus,
      units,
      enemyDetection,
      enemyStrength,
      enemyVision,
      friendlyStrength,
      friendlyVision,
      buildable,
      buildableTerrain,
      walkable,
      walkableTerran,
      walkableUnits,
      mobility
    ).foreach(_.update())
  }
}
