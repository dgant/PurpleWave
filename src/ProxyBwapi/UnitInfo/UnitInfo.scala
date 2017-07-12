package ProxyBwapi.UnitInfo

import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Performance.Caching.CacheFrame
import ProxyBwapi.Engine.Damage
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClass.UnitClass
import bwapi._

import scala.collection.mutable

abstract class UnitInfo (base: bwapi.Unit) extends UnitProxy(base) {
  
  //////////////
  // Identity //
  //////////////
  
  def friendly  : Option[FriendlyUnitInfo]  = None
  def foreign   : Option[ForeignUnitInfo]   = None
  
  override def toString: String = {
    unitClass.toString + " " +
    hitPoints + "/" + unitClass.maxHitPoints + " " +
    (if (shieldPoints > 0) "(" + shieldPoints + "/" + unitClass.maxShields + ") " else "") +
    tileIncludingCenter.toString + " " + pixelCenter.toString
  }
  
  def is(unitClasses: UnitClass*):Boolean = unitClasses.contains(unitClass)
  
  //////////////////
  // Statefulness //
  //////////////////

  private val history = new mutable.Queue[UnitState]
  def update() {
    if (history.headOption.exists(_.frame == With.frame)) {
      // Game is paused; we don't know how to clear the queue
      return
    }
      
    while (history.headOption.exists(_.age > With.configuration.unitHistoryAge)) {
      history.dequeue()
    }
    history.enqueue(new UnitState(this))
  }
  
  def lastAttackFrame: Int = {
    history.filter(_.attackStarting).lastOption.map(_.frame).getOrElse(0)
  }
  
  def damageInLastSecond: Int = damageInLastSectiondCache.get
  
  private val damageInLastSectiondCache = new CacheFrame(() =>
    Math.max(
      0,
      history
        .filter(_.age > 24)
        .lastOption
        .map(lastState =>
          lastState.hitPoints             - hitPoints     +
          lastState.shieldPoints          - shieldPoints  +
          lastState.defensiveMatrixPoints - defensiveMatrixPoints)
        .getOrElse(0)))
  
  private lazy val stuckMoveFrames     = 12
  private lazy val stuckAttackFrames = cooldownMaxAirGround + 8
  private lazy val stuckFramesMax    = Math.max(stuckMoveFrames, stuckAttackFrames)
  def seeminglyStuck: Boolean = {
    val recentHistory = history.takeRight(stuckFramesMax)
    history.size >= stuckFramesMax && (
      history.takeRight(stuckMoveFrames   ).forall(state => state.couldMoveThisFrame    && state.tryingToMove   && state.pixelCenter == pixelCenter) ||
      history.takeRight(stuckAttackFrames ).forall(state => state.couldAttackThisFrame  && state.tryingToAttack && state.pixelCenter == pixelCenter && state.cooldown == 0)
    )
  }
  
  ////////////
  // Health //
  ////////////
  
  def aliveAndComplete:Boolean = alive && complete
  
  def energyMax     : Int = unitClass.maxEnergy //TODO: Add upgrades
  def mineralsLeft  : Int = if (unitClass.isMinerals) resourcesLeft else 0
  def gasLeft       : Int = if (unitClass.isGas)      resourcesLeft else 0
  
  def wounded:Boolean = totalHealth < Math.min(With.configuration.woundedThresholdHealth, unitClass.maxTotalHealth/3)
  
  ///////////////
  // Economics //
  ///////////////
  
  def subjectiveValue: Int = unitClass.subjectiveValue + scarabCount * Protoss.Scarab.subjectiveValue + interceptorCount * Protoss.Interceptor.subjectiveValue
  
  //////////////
  // Geometry //
  //////////////
  
  def x: Int = pixelCenter.x
  def y: Int = pixelCenter.y
  
  def tileIncludingCenter:  Tile          = pixelCenter.tileIncluding
  def tileArea:             TileRectangle = unitClass.tileArea.add(tileTopLeft)
  
  def pixelRangeAir: Double = pixelRangeAirCache.get
  private val pixelRangeAirCache = new CacheFrame(() =>
    unitClass.airRange +
      (if (is(Terran.Bunker))                                                 32.0 else 0.0) +
      (if (is(Terran.Bunker)    && player.hasUpgrade(Terran.MarineRange))     32.0 else 0.0) +
      (if (is(Terran.Marine)    && player.hasUpgrade(Terran.MarineRange))     32.0 else 0.0) +
      (if (is(Terran.Goliath)   && player.hasUpgrade(Terran.GoliathAirRange)) 96.0 else 0.0) +
      (if (is(Protoss.Dragoon)  && player.hasUpgrade(Protoss.DragoonRange))   64.0 else 0.0) +
      (if (is(Zerg.Hydralisk)   && player.hasUpgrade(Zerg.HydraliskRange))    32.0 else 0.0))
  
  def pixelRangeGround: Double = pixelRangeGroundCache.get
  private val pixelRangeGroundCache = new CacheFrame(() =>
    unitClass.groundRange +
      (if (is(Terran.Bunker))                                               32.0 else 0.0) +
      (if (is(Terran.Bunker)    && player.hasUpgrade(Terran.MarineRange))   32.0 else 0.0) +
      (if (is(Terran.Marine)    && player.hasUpgrade(Terran.MarineRange))   32.0 else 0.0) +
      (if (is(Protoss.Dragoon)  && player.hasUpgrade(Protoss.DragoonRange)) 64.0 else 0.0) +
      (if (is(Zerg.Hydralisk)   && player.hasUpgrade(Zerg.HydraliskRange))  32.0 else 0.0))
  
  def pixelRangeMax:Double = Math.max(pixelRangeAir, pixelRangeGround)
  
  def canTraverse             (tile:        Tile)       : Boolean = flying || With.grids.walkable.get(tile)
  def pixelsFromEdgeSlow      (otherUnit:   UnitInfo)   : Double  = pixelDistanceSlow(otherUnit) - unitClass.radialHypotenuse - otherUnit.unitClass.radialHypotenuse
  def pixelsFromEdgeFast      (otherUnit:   UnitInfo)   : Double  = pixelDistanceFast(otherUnit) - unitClass.radialHypotenuse - otherUnit.unitClass.radialHypotenuse
  def pixelDistanceSlow       (otherPixel:  Pixel)      : Double  = pixelCenter.pixelDistanceSlow(otherPixel)
  def pixelDistanceSlow       (otherUnit:   UnitInfo)   : Double  = pixelDistanceSlow(otherUnit.pixelCenter)
  def pixelDistanceFast       (otherPixel:  Pixel)      : Double  = pixelCenter.pixelDistanceFast(otherPixel)
  def pixelDistanceFast       (otherUnit:   UnitInfo)   : Double  = pixelDistanceFast(otherUnit.pixelCenter)
  def pixelDistanceSquared    (otherUnit:   UnitInfo)   : Double  = pixelDistanceSquared(otherUnit.pixelCenter)
  def pixelDistanceSquared    (otherPixel:  Pixel)      : Double  = pixelCenter.pixelDistanceSquared(otherPixel)
  def pixelDistanceTravelling (destination: Pixel)      : Double  = pixelDistanceTravelling(pixelCenter, destination)
  def pixelDistanceTravelling (destination: Tile)       : Double  = pixelDistanceTravelling(pixelCenter, destination.pixelCenter)
  def pixelDistanceTravelling (from: Pixel, to: Pixel)  : Double  = if (flying) from.pixelDistanceFast(to) else from.groundPixels(to)
  
  def canMoveThisFrame: Boolean = unitClass.canMove && topSpeed > 0 && canDoAnythingThisFrame && ! burrowed
  
  def topSpeed: Double = topSpeedCache.get
  private val topSpeedCache = new CacheFrame(() =>
    (if (stimmed) 1.5 else 1.0) * (
    unitClass.topSpeed * (if (
      (is(Terran.Vulture)   && player.getUpgradeLevel(Terran.VultureSpeed)    > 0) ||
      (is(Protoss.Observer) && player.getUpgradeLevel(Protoss.ObserverSpeed)  > 0) ||
      (is(Protoss.Scout)    && player.getUpgradeLevel(Protoss.ScoutSpeed)     > 0) ||
      (is(Protoss.Shuttle)  && player.getUpgradeLevel(Protoss.ShuttleSpeed)   > 0) ||
      (is(Protoss.Zealot)   && player.getUpgradeLevel(Protoss.ZealotSpeed)    > 0) ||
      (is(Zerg.Overlord)    && player.getUpgradeLevel(Zerg.OverlordSpeed)     > 0) ||
      (is(Zerg.Zergling)    && player.getUpgradeLevel(Zerg.ZerglingSpeed)     > 0) ||
      (is(Zerg.Hydralisk)   && player.getUpgradeLevel(Zerg.HydraliskSpeed)    > 0) ||
      (is(Zerg.Ultralisk)   && player.getUpgradeLevel(Zerg.UltraliskSpeed)    > 0))
      1.5 else 1.0)))
  
  def project(distance: Double)       : Pixel = pixelCenter.radiateRadians(angleRadians, distance)
  def project(framesToLookAhead: Int) : Pixel = pixelCenter.add((velocityX * framesToLookAhead).toInt, (velocityY * framesToLookAhead).toInt)
  
  def inTileRadius  (tiles: Int)  : Traversable[UnitInfo] = With.units.inTileRadius(tileIncludingCenter, tiles)
  def inPixelRadius (pixels: Int) : Traversable[UnitInfo] = With.units.inPixelRadius(pixelCenter, pixels)
  
  ////////////
  // Combat //
  ////////////
  
  def battle: Option[Battle] = With.battles.byUnit.get(this)
  
  def melee: Boolean = unitClass.maxAirGroundRange <= 32 * 2
  
  //TODO: Account for upgrades. Make sure to handle case where unit has no armor upgrades
  def armorHealth: Int = unitClass.armor // if (player.getUpgradeLevel(unitClass.armorUpgrade)
  def armorShield: Int = 0 //if(unitClass.maxShields > 0) player.getUpgradeLevel(Protoss.Shields) else 0
  
  def totalHealth: Int = hitPoints + shieldPoints + defensiveMatrixPoints
  def fractionalHealth:Double = totalHealth.toDouble / unitClass.maxTotalHealth
  
  def stimAttackSpeedBonus: Int = if (stimmed) 2 else 1
  
  def attacksGround : Boolean = unitClass.attacksGround
  def attacksAir    : Boolean = unitClass.attacksAir
  
  def airDps    : Double = stimAttackSpeedBonus * unitClass.airDps
  def groundDps : Double = stimAttackSpeedBonus * unitClass.groundDps
  
  def attacksAgainstAir: Int = unitClass.airDamageFactorRaw * unitClass.maxAirHitsRaw
  def attacksAgainstGround: Int = {
    var output = unitClass.groundDamageFactorRaw * unitClass.maxGroundHitsRaw
    //if() is just to avoid slow is() calls
    if (output == 0) {
      if (is(Protoss.Reaver)) output = 1
      //TODO: Carrier = N attacks, but not Nx damage
      //TODO: Bunker = 4 attacks, but not 4x damage
    }
    output
  }
  
  def cooldownLeft          : Int         = Math.max(airCooldownLeft, groundCooldownLeft)
  def cooldownMaxAir        : Int         = unitClass.airDamageCooldown     / stimAttackSpeedBonus
  def cooldownMaxGround     : Int         = unitClass.groundDamageCooldown  / stimAttackSpeedBonus
  def cooldownMaxAirGround  : Int         = Math.max(if (attacksAir) cooldownMaxAir else 0, if (attacksGround) cooldownMaxGround else 0)
  def cooldownMaxAgainst(enemy:UnitInfo): Int = if (enemy.flying) cooldownMaxAir else cooldownMaxGround
  
  def pixelRangeAgainstFromEdge   (enemy:UnitInfo): Double =  if (enemy.flying) pixelRangeAir else pixelRangeGround
  def pixelRangeAgainstFromCenter (enemy:UnitInfo): Double = (if (enemy.flying) pixelRangeAir else pixelRangeGround) + unitClass.radialHypotenuse + enemy.unitClass.radialHypotenuse
  
  def damageTypeAgainst (enemy:UnitInfo)  : DamageType  = if (enemy.flying) unitClass.airDamageTypeRaw    else unitClass.groundDamageTypeRaw
  def attacksAgainst    (enemy:UnitInfo)  : Int         = if (enemy.flying) attacksAgainstAir             else attacksAgainstGround
  
  def damageScaleAgainst(enemy:UnitInfo): Double =
    if (enemy.flying && airDps > 0)
      if (enemy.shieldPoints > 5) 1.0
      else Damage.scaleBySize(unitClass.airDamageTypeRaw, enemy.unitClass.size)
    else if (groundDps > 0)
      if (enemy.shieldPoints > 5) 1.0
      else Damage.scaleBySize(unitClass.groundDamageTypeRaw, enemy.unitClass.size)
    else
      0.0
  
  def damageOnHitBeforeArmorGround : Int = unitClass.effectiveGroundDamage //Plus upgrades! Note that the base method accounts for multiple hits (ie interceptors, bunker) and needs revision for this to be accurately used vs armor
  def damageOnHitBeforeArmorAir    : Int = unitClass.effectiveAirDamage    //Plus upgrades! Note that the base method accounts for multiple hits (ie interceptors, bunker) and needs revision for this to be accurately used vs armor
  def damageOnHitBeforeArmor(enemy:UnitInfo):Int = if(enemy.flying) damageOnHitBeforeArmorAir else damageOnHitBeforeArmorGround
  
  def damageAgainst(enemy:UnitInfo, enemyShields:Int = 0) : Int = {
    val hits = attacksAgainst(enemy)
    val damageOnHit = damageOnHitBeforeArmor(enemy:UnitInfo)
    val damageScale = damageScaleAgainst(enemy)
    val damageToShields = if (enemy.shieldPoints > 0) Math.max(0, Math.min(enemy.shieldPoints, hits * (damageOnHit - enemy.armorShield))) else 0
    val damageToHealth  = Math.max(0, damageScale * (hits * (damageOnHit - enemy.armorHealth) - damageToShields))
    Math.max(1, damageToHealth.toInt + damageToShields)
  }
  
  def dpsAgainst(enemy:UnitInfo): Double = {
    val cooldownVs = cooldownMaxAgainst(enemy)
    if (cooldownVs == 0) return 0.0
    damageAgainst(enemy) * 24.0 / cooldownVs
  }
  
  def canDoAnythingThisFrame:Boolean = canDoAnythingThisFrameCache.get
  private val canDoAnythingThisFrameCache = new CacheFrame(() =>
    aliveAndComplete  &&
      true)
  
    // These three checks along comprise 6% of our CPU usage. Yes, really.
    // ! stasised        &&
    // ! maelstrommed    &&
    // ! lockedDown)
  
  def canBeAttackedThisFrame:Boolean = canBeAttackedThisFrameCache.get
  private val canBeAttackedThisFrameCache = new CacheFrame(() =>
      alive &&
      (complete || unitClass.isBuilding) &&
      totalHealth > 0 &&
      visible &&
      ! invincible &&
      ! stasised)
  
  def canAttackThisSecond: Boolean = canAttackThisSecondCache.get
  private val canAttackThisSecondCache = new CacheFrame(() =>
    canDoAnythingThisFrame &&
    (
      unitClass.canAttack
      || (is(Terran.Bunker)
      || (is(Protoss.Carrier) && interceptorCount > 0)
      || (is(Protoss.Reaver)  && scarabCount > 0)
      || (is(Zerg.Lurker)     && burrowed)
    )))
  
  def canAttackThisSecond(enemy: UnitInfo): Boolean =
    canAttackThisSecond &&
    enemy.canBeAttackedThisFrame &&
    ! enemy.effectivelyCloaked &&
    (if (enemy.flying) unitClass.attacksAir else unitClass.attacksGround)
  
  def canAttackThisFrame:Boolean = canAttackThisSecond && cooldownLeft <= With.latency.framesRemaining
  
  def pixelsCovered     (framesAhead: Int): Double = if (canMoveThisFrame) topSpeed * framesAhead else 0.0
  def pixelReachAir     (framesAhead: Int): Double = pixelsCovered(framesAhead) + pixelRangeAir
  def pixelReachGround  (framesAhead: Int): Double = pixelsCovered(framesAhead) + pixelRangeGround
  def pixelReachMax     (framesAhead: Int): Double = Math.max(pixelReachAir(framesAhead), pixelReachGround(framesAhead))
  def pixelReachAgainst (framesAhead: Int, enemy:UnitInfo): Double = if (enemy.flying) pixelReachAir(framesAhead) else pixelReachGround(framesAhead)
  
  def inRangeToAttackSlow(enemy: UnitInfo) : Boolean = pixelsFromEdgeSlow(enemy) <= pixelRangeAgainstFromEdge(enemy) + With.configuration.attackableRangeBuffer
  def inRangeToAttackFast(enemy: UnitInfo) : Boolean = pixelsFromEdgeFast(enemy) <= pixelRangeAgainstFromEdge(enemy) + With.configuration.attackableRangeBuffer
  
  def inRangeToAttackSlow(enemy: UnitInfo, framesAhead: Int) : Boolean = enemy.project(framesAhead).pixelDistanceSlow(project(framesAhead)) <= pixelRangeAgainstFromEdge(enemy) + unitClass.radialHypotenuse + enemy.unitClass.radialHypotenuse + With.configuration.attackableRangeBuffer
  def inRangeToAttackFast(enemy: UnitInfo, framesAhead: Int) : Boolean = enemy.project(framesAhead).pixelDistanceFast(project(framesAhead)) <= pixelRangeAgainstFromEdge(enemy) + unitClass.radialHypotenuse + enemy.unitClass.radialHypotenuse + With.configuration.attackableRangeBuffer
  
  def framesToTravel(destination: Pixel)    : Int = framesToTravelPixels(pixelDistanceTravelling(destination))
  def framesToTravelPixels(pixels: Double)  : Int = if (canMoveThisFrame) Math.max(0, Math.ceil(pixels/topSpeed).toInt) else Int.MaxValue
  def framesBeforeAttacking(enemy: UnitInfo): Int = {
    if (canAttackThisSecond(enemy)) {
      Math.max(cooldownLeft, framesToTravelPixels(pixelDistanceFast(enemy) - pixelRangeAgainstFromEdge(enemy)))
    }
    else Int.MaxValue
  }
  
  def framesPerAttack: Int = 10 //TODO: Fill in with values from Dave Churchill's table
  
  ////////////
  // Orders //
  ////////////
  
  def gathering: Boolean = gatheringMinerals || gatheringGas
  
  def carryingResources:Boolean = carryingMinerals || carryingGas
  
  def isBeingViolent: Boolean = {
    attacking ||
    target.orElse(orderTarget).exists(isEnemyOf) ||
    List(Commands.Attack_Move, Commands.Attack_Unit).contains(command.map(_.getUnitCommandType.toString).getOrElse(""))
  }
  
  def isBeingViolentTo(victim: UnitInfo): Boolean = {
    //Are we capable of hurting the victim?
    isEnemyOf(victim) &&
    canAttackThisSecond(victim) &&
    //Are we not attacking anyone else?
    ! target.orElse(orderTarget).exists(_ != victim) &&
    //Are we close to being able to hit the victim?
    victim.pixelDistanceFast(
      pixelCenter.project(
        targetPixel
          .orElse(orderTargetPixel)
          .getOrElse(pixelCenter.radiateRadians(angleRadians, 10)),
        Math.min(pixelDistanceFast(victim), topSpeed * With.configuration.violenceFrameThreshold))) <=
      pixelRangeAgainstFromEdge(victim)
  }
  
  ////////////////
  // Visibility //
  ////////////////
  
  def likelyStillThere: Boolean =
    possiblyStillThere &&
    ( ! canMoveThisFrame || With.framesSince(lastSeen) < With.configuration.fogPositionDuration || is(Terran.SiegeTankUnsieged))
  
  def effectivelyCloaked:Boolean =
    (burrowed || cloaked) && (
      if (isFriendly) ! With.grids.enemyDetection.get(tileIncludingCenter)
      else            ! detected
    )
  
  /////////////
  // Players //
  /////////////
  
  def isOurs     : Boolean = player.isUs
  def isNeutral  : Boolean = player.isNeutral
  def isFriendly : Boolean = player.isAlly || isOurs
  def isEnemy    : Boolean = player.isEnemy
  def isEnemyOf(otherUnit:UnitInfo): Boolean = (isFriendly && otherUnit.isEnemy) || (isEnemy && otherUnit.isFriendly)
}
