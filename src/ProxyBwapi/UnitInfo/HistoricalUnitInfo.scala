package ProxyBwapi.UnitInfo
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitTracking.Visibility
import ProxyBwapi.Upgrades.Upgrade

class HistoricalUnitInfo(unit: UnitInfo) extends UnitInfo(unit.bwapiUnit, unit.id) {
  override val visibility: Visibility.Value = Visibility.Dead
  override val player: PlayerInfo = unit.player
  override val lastSeen: Int = unit.lastSeen
  override val alive: Boolean = false
  override val complete: Boolean = unit.complete
  override val matrixPoints: Int = unit.matrixPoints
  override val hitPoints: Int = unit.hitPoints
  override val initialResources: Int = unit.initialResources
  override val invincible: Boolean = unit.invincible
  override val resourcesLeft: Int = unit.resourcesLeft
  override val shieldPoints: Int = unit.shieldPoints
  override val energy: Int = unit.energy
  override val unitClass: UnitClass = unit.unitClass
  override val plagued: Boolean = unit.plagued
  override val interceptors: Seq[UnitInfo] = Seq.empty
  override val scarabs: Int = unit.scarabs
  override val spiderMines: Int = unit.spiderMines
  override val cooldownAir: Int = unit.cooldownAir
  override val cooldownGround: Int = unit.cooldownGround
  override val cooldownSpell: Int = unit.cooldownSpell
  override val pixel: Pixel = unit.pixel
  override val tileTopLeft: Tile = unit.tileTopLeft
  override val target: Option[UnitInfo] = None
  override val targetPixel: Option[Pixel] = unit.targetPixel
  override val orderTarget: Option[UnitInfo] = None
  override val orderTargetPixel: Option[Pixel] = unit.orderTargetPixel
  override val order: String = unit.order
  override val constructing: Boolean = unit.constructing
  override val morphing: Boolean = unit.morphing
  override val repairing: Boolean = unit.repairing
  override val teching: Boolean = unit.teching
  override val training: Boolean = unit.training
  override val upgrading: Boolean = unit.upgrading
  override val burrowed: Boolean = unit.burrowed
  override val cloaked: Boolean = unit.cloaked
  override val detected: Boolean = unit.detected
  override val visible: Boolean = unit.visible
  override val angleRadians: Double = unit.angleRadians
  override val ensnared: Boolean = unit.ensnared
  override val flying: Boolean = unit.flying
  override val irradiated: Boolean = unit.irradiated
  override val lockedDown: Boolean = unit.lockedDown
  override val maelstrommed: Boolean = unit.maelstrommed
  override val stasised: Boolean = unit.stasised
  override val stimmed: Boolean = unit.stimmed
  override val velocityX: Double = unit.velocityX
  override val velocityY: Double = unit.velocityY
  override val remainingCompletionFrames: Int = unit.remainingCompletionFrames
  override val remainingUpgradeFrames: Int = unit.remainingUpgradeFrames
  override val remainingTechFrames: Int = unit.remainingTechFrames
  override val remainingTrainFrames: Int = unit.remainingTrainFrames
  override val beingHealed: Boolean = unit.beingHealed
  override val blind: Boolean = unit.blind
  override val carryingMinerals: Boolean = unit.carryingMinerals
  override val carryingGas: Boolean = unit.carryingGas
  override val powered: Boolean = unit.powered
  override val selected: Boolean = unit.selected
  override val underDarkSwarm: Boolean = unit.underDarkSwarm
  override val underDisruptionWeb: Boolean = unit.underDisruptionWeb
  override val underStorm: Boolean = unit.underStorm
  override val addon: Option[UnitInfo] = None
  override val hasNuke: Boolean = unit.hasNuke
  override val removalFrames: Int = 0
  override val techProducing: Option[Tech] = unit.techProducing
  override val upgradeProducing: Option[Upgrade]  = unit.upgradeProducing
  override val tile: Tile = unit.tile
  override val loaded: Boolean = unit.loaded
  override val pixelObserved: Pixel = unit.pixelObserved
  override val beingConstructed: Boolean = unit.beingConstructed
  override val beingGathered: Boolean = unit.beingGathered
  override val cooldownRaw: Int = unit.cooldownRaw
  override val buildType: UnitClass = unit.buildType
  override val trainingQueue: Seq[UnitClass] = Seq.empty
  override val loadedUnitCount: Int = 0
}