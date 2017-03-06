package Global.Information

import Geometry.Grids.Abstract.GridUnits
import Geometry.Grids.Real._

class Grids {
  val units = new GridUnits
  val enemyGroundStrength = new GridEnemyGroundStrength
  val friendlyGroundStrength = new GridFriendlyGroundStrength
  val enemyVision = new GridEnemyVision
  val altitudeBonus = new GridAltitudeBonus
  val buildability = new GridBuildability
  val walkability = new GridWalkability
  val mobility = new GridMobility
  
  def onFrame() {
    units.update()
    enemyGroundStrength.update()
    friendlyGroundStrength.update()
    enemyVision.update()
    altitudeBonus.update()
    buildability.update()
    walkability.update()
    mobility.update()
  }
}
