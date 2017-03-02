package Global.Information

import Geometry.Grids.Real._

class Maps {
  val enemyGroundStrength = new GridEnemyGroundStrength
  val friendlyGroundStrength = new GridFriendlyGroundStrength
  val enemyVision = new GridEnemyVision
  val altitudeBonus = new GridAltitudeBonus
  val walkability = new GridWalkability
  val mobility = new GridMobility
  
  def onFrame() {
    enemyGroundStrength.update()
    friendlyGroundStrength.update()
    enemyVision.update()
    altitudeBonus.update()
    walkability.update()
    mobility.update()
  }
}
