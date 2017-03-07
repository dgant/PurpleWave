package Types.UnitInfo
import Startup.With
import Utilities.Enrichment.EnrichPosition._
import bwapi._

import scala.collection.JavaConverters._

class FriendlyUnitInfo(_baseUnit:bwapi.Unit) extends UnitInfo(_baseUnit) {
  override def lastSeen                   : Int                 = With.game.getFrameCount
  override def possiblyStillThere         : Boolean             = true
  override def alive                      : Boolean             = baseUnit.exists
  override def player                     : Player              = baseUnit.getPlayer
  override def position                   : Position            = baseUnit.getPosition
  override def walkPosition               : WalkPosition        = position.toWalkPosition
  override def tileTopLeft                : TilePosition        = baseUnit.getTilePosition
  override def hitPoints                  : Int                 = baseUnit.getHitPoints
  override def shieldPoints               : Int                 = baseUnit.getShields
  override def utype                      : UnitType            = baseUnit.getType
  override def complete                   : Boolean             = baseUnit.isCompleted
  override def flying                     : Boolean             = baseUnit.isFlying
  override def visible                    : Boolean             = baseUnit.isVisible
  override def cloaked                    : Boolean             = baseUnit.isCloaked
  override def top                        : Int                 = baseUnit.getTop
  override def left                       : Int                 = baseUnit.getLeft
  override def right                      : Int                 = baseUnit.getRight
  override def bottom                     : Int                 = baseUnit.getBottom
  def isCarryingMinerals                  : Boolean             = baseUnit.isCarryingMinerals
  def isCarryingGas                       : Boolean             = baseUnit.isCarryingGas
  def isGatheringMinerals                 : Boolean             = baseUnit.isGatheringMinerals
  def isGatheringGas                      : Boolean             = baseUnit.isGatheringGas
  def isMoving                            : Boolean             = baseUnit.isMoving
  def command                             : UnitCommand         = baseUnit.getLastCommand
  def cooldownRemaining                   : Int                 = Math.max(baseUnit.getGroundWeaponCooldown, baseUnit.getAirWeaponCooldown)
  def onCooldown                          : Boolean             = cooldownRemaining > 0
  def getBuildUnit                        : Option[UnitInfo]    = With.units.getUnit(baseUnit.getBuildUnit)
  def trainingQueue                       : Iterable[UnitType]  = baseUnit.getTrainingQueue.asScala
  def teching                             : TechType            = baseUnit.getTech
  def upgrading                           : UpgradeType         = baseUnit.getUpgrade
  def order                               : Order               = baseUnit.getOrder
}
