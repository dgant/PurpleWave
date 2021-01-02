package ProxyBwapi.UnitInfo
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitTracking.Visibility
import ProxyBwapi.Upgrades.Upgrade

class HistoricalUnitInfo(unit: UnitInfo) extends UnitInfo(unit.baseUnit, unit.id) {
  override def visibility: Visibility.Value = Visibility.Dead
  override val player: PlayerInfo = unit.player
  override val lastSeen: Int = unit.lastSeen
  override val alive: Boolean = false
  override val complete: Boolean = unit.complete
  override val defensiveMatrixPoints: Int = unit.defensiveMatrixPoints
  override val hitPoints: Int = unit.hitPoints
  override val initialResources: Int = unit.initialResources
  override val invincible: Boolean = unit.invincible
  override val resourcesLeft: Int = unit.resourcesLeft
  override val shieldPoints: Int = unit.shieldPoints
  override val energy: Int = unit.energy
  override val unitClass: UnitClass = unit.unitClass
  override val plagued: Boolean = unit.plagued
  override val interceptors: Iterable[UnitInfo] = Iterable.empty
  override val interceptorCount: Int = unit.interceptorCount
  override val scarabCount: Int = unit.scarabCount
  override val spiderMines: Int = unit.spiderMines
  override val attackStarting: Boolean = unit.attackStarting
  override val attackAnimationHappening: Boolean = unit.attackAnimationHappening
  override val airCooldownLeft: Int = unit.airCooldownLeft
  override val groundCooldownLeft: Int = unit.groundCooldownLeft
  override val spellCooldownLeft: Int = unit.spellCooldownLeft
  override val pixel: Pixel = unit.pixel
  override val tileTopLeft: Tile = unit.tileTopLeft
  override val gatheringMinerals: Boolean = unit.gatheringMinerals
  override val gatheringGas: Boolean = unit.gatheringGas
  override val target: Option[UnitInfo] = None
  override val targetPixel: Option[Pixel] = unit.targetPixel
  override val orderTarget: Option[UnitInfo] = None
  override val orderTargetPixel: Option[Pixel] = unit.orderTargetPixel
  override val order: String = unit.order
  override val attacking: Boolean = unit.attacking
  override val constructing: Boolean = unit.constructing
  override val following: Boolean = unit.following
  override val holdingPosition: Boolean = unit.holdingPosition
  override val idle: Boolean = unit.idle
  override val interruptible: Boolean = unit.interruptible
  override val morphing: Boolean = unit.morphing
  override val repairing: Boolean = unit.repairing
  override val teching: Boolean = unit.teching
  override val patrolling: Boolean = unit.patrolling
  override val training: Boolean = unit.training
  override val upgrading: Boolean = unit.upgrading
  override val burrowed: Boolean = unit.burrowed
  override val cloaked: Boolean = unit.cloaked
  override val detected: Boolean = unit.detected
  override val visible: Boolean = unit.visible
  override val accelerating: Boolean = unit.accelerating
  override val angleRadians: Double = unit.angleRadians
  override val braking: Boolean = unit.braking
  override val ensnared: Boolean = unit.ensnared
  override val flying: Boolean = unit.flying
  override val irradiated: Boolean = unit.irradiated
  override val lifted: Boolean = unit.lifted
  override val lockedDown: Boolean = unit.lockedDown
  override val maelstrommed: Boolean = unit.maelstrommed
  override val sieged: Boolean = unit.sieged
  override val stasised: Boolean = unit.stasised
  override val stimmed: Boolean = unit.stimmed
  override val stuck: Boolean = unit.stuck
  override val velocityX: Double = unit.velocityX
  override val velocityY: Double = unit.velocityY
  override val remainingCompletionFrames: Int = unit.remainingCompletionFrames
  override val remainingUpgradeFrames: Int = unit.remainingUpgradeFrames
  override val remainingTechFrames: Int = unit.remainingTechFrames
  override val remainingTrainFrames: Int = unit.remainingTrainFrames
  override val beingConstructed: Boolean = unit.beingConstructed
  override val beingGathered: Boolean = unit.beingGathered
  override val beingHealed: Boolean = unit.beingHealed
  override val blind: Boolean = unit.blind
  override val carryingMinerals: Boolean = unit.carryingMinerals
  override val carryingGas: Boolean = unit.carryingGas
  override val powered: Boolean = unit.powered
  override val selected: Boolean = unit.selected
  override val targetable: Boolean = unit.targetable
  override val underAttack: Boolean = unit.underAttack
  override val underDarkSwarm: Boolean = unit.underDarkSwarm
  override val underDisruptionWeb: Boolean = unit.underDisruptionWeb
  override val underStorm: Boolean = unit.underStorm
  override val addon: Option[UnitInfo] = None
  override val hasNuke: Boolean = unit.hasNuke
  override val framesUntilRemoval: Int = 0
  override val techProducing: Option[Tech] = unit.techProducing
  override val upgradeProducing: Option[Upgrade]  = unit.upgradeProducing
  override val unitProducing: Option[UnitClass] = unit.unitProducing
}