package ProxyBwapi.Bullets

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Physics.Force
import Mathematics.Points.Pixel
import ProxyBwapi.Players.{PlayerInfo, Players}
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.{Bullet, BulletType}

case class BulletInfo(bwapi: Bullet) {
  val id                    : Int               = bwapi.getID
  val exists                : Boolean           = bwapi.exists
  lazy val player           : PlayerInfo        = Players.get(bwapi.getPlayer)
  lazy val pixel            : Pixel             = new Pixel(bwapi.getPosition)
  lazy val sourceUnit       : Option[UnitInfo]  = With.units.get(bwapi.getSource)
  lazy val targetUnit       : Option[UnitInfo]  = With.units.get(bwapi.getTarget)
  lazy val targetPixel      : Option[Pixel]     = Some(new Pixel(bwapi.getTargetPosition)).filter(_.valid)
  lazy val angleRadians     : Double            = bwapi.getAngle
  lazy val bulletType       : BulletType        = bwapi.getType
  lazy val velocityX        : Double            = bwapi.getVelocityX
  lazy val velocityY        : Double            = bwapi.getVelocityY
  lazy val velocity         : Force             = Force(velocityX, velocityY)
  lazy val speed            : Double            = Maff.broodWarDistanceDouble(0.0, 0.0, velocityX, velocityY)
  lazy val moving           : Boolean           = distanceToTarget > 0
  lazy val distanceToTarget : Double            = pixel.pixelDistance(targetPixel.orElse(targetUnit.map(_.pixel)).getOrElse(pixel))
  lazy val framesRemaining  : Int               = bwapi.getRemoveTimer
  lazy val framesUntilBoom  : Int               = if (framesRemaining > 0) framesRemaining else Maff.nanToZero(distanceToTarget / speed).toInt
}
