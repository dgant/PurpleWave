package Global.Information

import Geometry.Field.{MapEnemyGroundDamage, MapFriendlyGroundDamage, MapMobility, MapWalkability}

class Maps {
  val enemyGroundDamage = new MapEnemyGroundDamage
  val friendlyGroundDamage = new MapFriendlyGroundDamage
  val walkability = new MapWalkability
  val mobility = new MapMobility
  
  def onFrame() {
    enemyGroundDamage.update()
    friendlyGroundDamage.update()
    walkability.update()
    mobility.update()
  }
}
