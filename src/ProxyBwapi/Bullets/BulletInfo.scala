package ProxyBwapi.Bullets

import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.{Bullet, BulletType}

class BulletInfo(bwapi: Bullet) {
  lazy val id               : Int               = bwapi.getID
  lazy val exists           : Boolean           = bwapi.exists
  lazy val player           : PlayerInfo        = Players.get(bwapi.getPlayer)
  lazy val pixel            : Pixel             = new Pixel(bwapi.getPosition)
  lazy val sourceUnit       : Option[UnitInfo]  = With.units.get(bwapi.getSource)
  lazy val targetUnit       : Option[UnitInfo]  = With.units.get(bwapi.getTarget)
  lazy val targetPixel      : Pixel             = new Pixel(bwapi.getTargetPosition)
  lazy val angleRadians     : Double            = bwapi.getAngle
  lazy val bulletType       : BulletType        = bwapi.getType
  lazy val velocityX        : Double            = bwapi.getVelocityX
  lazy val velocityY        : Double            = bwapi.getVelocityY
  lazy val framesRemaining  : Int               = bwapi.getRemoveTimer
}
