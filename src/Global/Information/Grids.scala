package Global.Information

import Geometry.Grids.Real._

class Grids {
  val enemyGroundStrength = new GridEnemyGroundStrength
  val friendlyGroundStrength = new GridFriendlyGroundStrength
  val enemyVision = new GridEnemyVision
  val altitudeBonus = new GridAltitudeBonus
  val buildability = new GridBuildability
  val walkability = new GridWalkability
  val mobility = new GridMobility
  
  def onFrame() {
    enemyGroundStrength.update()
    friendlyGroundStrength.update()
    enemyVision.update()
    altitudeBonus.update()
    buildability.update()
    walkability.update()
    mobility.update()
  }
}
