package Global.Information

import Geometry.Influence._

class Influence {
  val enemySmallGroundDamage  = new MapEnemySmallGroundDamage
  val enemyMediumGroundDamage = new MapEnemyMediumGroundDamage
  val enemyLargeGroundDamage  = new MapEnemyLargeGroundDamage
  val enemySmallAirDamage     = new MapEnemySmallAirDamage
  val enemyMediumAirDamage    = new MapEnemyMediumAirDamage
  val enemyLargeAirDamage     = new MapEnemyLargeAirDamage
  val friendlyGroundDamage    = new MapFriendlyGroundDamage
  val friendlyAirDamage       = new MapFriendlyAirDamage
  val groundAttractors        = new MapGroundAttractors
  val enemyVision             = new MapEnemyVision
  
  def onFrame() = {
    enemySmallGroundDamage.update()
    enemyMediumGroundDamage.update()
    enemyLargeGroundDamage.update()
    enemySmallAirDamage.update()
    enemyMediumAirDamage.update()
    enemyLargeAirDamage.update()
    friendlyGroundDamage.update()
    friendlyAirDamage.update()
    groundAttractors.update()
    enemyVision.update()
  }
}
