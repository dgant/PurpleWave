package Types.UnitInfo
import Startup.With
import Utilities.Enrichment.EnrichPosition._
import bwapi._

import scala.collection.JavaConverters._

class FriendlyUnitInfo(_baseUnit:bwapi.Unit) extends UnitInfo(_baseUnit) {
  override def alive: Boolean = baseUnit.exists
  override def lastSeen: Int = With.game.getFrameCount
  override def possiblyStillThere: Boolean = true
  override def player: Player = baseUnit.getPlayer
  override def position: Position = baseUnit.getPosition
  override def walkPosition: WalkPosition = position.toWalkPosition
  override def tilePosition: TilePosition = baseUnit.getTilePosition
  override def hitPoints: Int = baseUnit.getHitPoints
  override def shieldPoints: Int = baseUnit.getShields
  override def unitType: UnitType = baseUnit.getType
  override def complete: Boolean = baseUnit.isCompleted
  override def flying:Boolean = baseUnit.isFlying
  override def visible:Boolean = baseUnit.isVisible
  override def cloaked: Boolean = baseUnit.isCloaked
  def isCarryingMinerals:Boolean = baseUnit.isCarryingMinerals
  def isCarryingGas:Boolean = baseUnit.isCarryingGas
  def isGatheringMinerals: Boolean = baseUnit.isGatheringMinerals
  def isGatheringGas:Boolean = baseUnit.isGatheringGas
  def isMoving:Boolean = baseUnit.isMoving
  def command:UnitCommand = baseUnit.getLastCommand
  def cooldownRemaining: Int = Math.max(baseUnit.getGroundWeaponCooldown, baseUnit.getAirWeaponCooldown)
  def getBuildUnit:Option[UnitInfo] = With.units.get(baseUnit.getBuildUnit)
  def trainingQueue:Iterable[UnitType] = baseUnit.getTrainingQueue.asScala
  def getTech:TechType = baseUnit.getTech
  def getUpgrade:UpgradeType = baseUnit.getUpgrade
}
