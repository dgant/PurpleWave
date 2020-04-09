package ProxyBwapi.UnitInfo

import Debugging.Visualizations.Colors
import Information.Battles.MCRS.MCRSUnit
import Information.Battles.Types.BattleLocal
import Information.Geography.Types.{Base, Zone}
import Information.Grids.AbstractGrid
import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Mathematics.PurpleMath
import Micro.Heuristics.EvaluateTargets
import Micro.Matchups.MatchupAnalysis
import Performance.Cache
import Planning.UnitMatchers.{UnitMatchSiegeTank, UnitMatcher}
import ProxyBwapi.Engine.Damage
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade
import Utilities.{ByOption, Forever, LightYear}
import bwapi._

abstract class UnitInfo(baseUnit: bwapi.Unit, id: Int) extends UnitProxy(baseUnit, id) {

  //////////////
  // Identity //
  //////////////
  
  def friendly  : Option[FriendlyUnitInfo]  = None
  def foreign   : Option[ForeignUnitInfo]   = None
  
  override def toString: String = (
    unitClass.toString
    + (if (selected) " (*)" else "")
    + " #" +
    id + " " +
    hitPoints + "/" + unitClass.maxHitPoints + " " +
    (if (shieldPoints > 0) "(" + shieldPoints + "/" + unitClass.maxShields + ") " else "") +
    tileIncludingCenter.toString + " " + pixelCenter.toString
    )

  @inline def is(unitMatcher: UnitMatcher): Boolean = unitMatcher.apply(this)
  @inline def isPrerequisite(unitMatcher: UnitMatcher): Boolean = (
    unitMatcher(this)
      || unitMatcher == Zerg.Hatchery && isAny(Zerg.Lair, Zerg.Hive)
      || unitMatcher == Zerg.Lair && is(Zerg.Hatchery)
      || unitMatcher == Zerg.Spire && is(Zerg.GreaterSpire))
  @inline def isNone(unitMatchers: UnitMatcher*): Boolean = ! unitMatchers.exists(_.apply(this))
  @inline def isAny(unitMatchers: UnitMatcher*): Boolean = unitMatchers.exists(_.apply(this))
  @inline def isAll(unitMatchers: UnitMatcher*): Boolean = unitMatchers.forall(_.apply(this))
  
  //////////////////
  // Statefulness //
  //////////////////

  val mcrs: MCRSUnit = new MCRSUnit(this)

  // Used in clustering; attached to unit for performance to avoid use of sets/maps
  var clusteringEnabled: Boolean = false

  val frameDiscovered           : Int = With.frame
  val initialHitPoints          : Int = baseUnit.getHitPoints
  val initialShields            : Int = baseUnit.getShields
  var completionFrame           : Int = Forever() // Can't use unitClass during construction
  var lastHitPoints             : Int = _
  var lastShieldPoints          : Int = _
  var lastDefensiveMatrixPoints : Int = _
  var lastCooldown              : Int = _
  var lastFrameTakingDamage     : Int = _
  var lastFrameTryingToMove     : Int = _
  var lastFrameTryingToAttack   : Int = _
  var lastFrameStartingAttack   : Int = _
  var lastFrameOccupied         : Int = _
  var framesFailingToMove       : Int = 0
  var framesFailingToAttack     : Int = 0
  var hasEverBeenCompleteHatch  : Boolean = false // Stupid AIST hack fix for detecting whether a base is mineable
  var lastAttacker              : Option[UnitInfo] = None
  var lastGatheringUpdate       : Int = Int.MinValue
  var discoveredByEnemy         : Boolean = false
  def lastTotalHealthPoints: Int = lastHitPoints + lastShieldPoints + lastDefensiveMatrixPoints

  private var lastUnitClass: UnitClass = _
  def updateCommon() {
    val thisFrame = With.frame
    if (totalHealth < lastTotalHealthPoints) {
      lastFrameTakingDamage = thisFrame
    }
    if (cooldownLeft > lastCooldown) {
      lastFrameStartingAttack = thisFrame
    }
    if (visibleToOpponents) {
      discoveredByEnemy = true
    }
    val moving = velocityX != 0 || velocityY != 0
    lazy val couldMove          = unitClass.canMove
    lazy val tryingToMove       = canMove && friendly.flatMap(_.agent.movingTo).exists(_.pixelDistance(pixelCenter) > 32)
    lazy val tryingToAttackHere = canAttack && target.exists(t => t.isEnemyOf(this) &&  inRangeToAttack(t))
    lazy val tryingToAttackAway = canAttack && target.exists(t => t.isEnemyOf(this) && ! inRangeToAttack(t))
    if ( ! moving && couldMove && (tryingToMove || tryingToAttackAway)) {
      framesFailingToMove += 1
    }
    else {
      framesFailingToMove = 0
    }
    if (cooldownLeft == 0 && tryingToAttackHere) {
      framesFailingToAttack += 1
    }
    else {
      framesFailingToAttack = 0
    }
    if (complete) {
      // If the unit class changes (eg. Geyser -> Extractor) update the completion frame
      if (unitClass != lastUnitClass) {
        completionFrame = With.frame
      }

      // We don't know exactly when it finished; now is our best guess.
      // The most important consumer of this estimate is fingerprinting.
      // For fingerprinting, "finished now" is a pretty decent metric.
      completionFrame = Math.min(completionFrame, With.frame)
    }
    // The latter case is for units that have *never* had an assigned completion time (eg. == Forever())
    if ( ! complete || completionFrame > 24 * 60 * 180) {
      completionFrame = With.frame + remainingCompletionFrames
    }
    if (remainingOccupationFrames > 0) {
      lastFrameOccupied = With.frame
    }
    lastUnitClass             = unitClass
    lastHitPoints             = hitPoints
    lastShieldPoints          = shieldPoints
    lastDefensiveMatrixPoints = defensiveMatrixPoints
    lastCooldown              = cooldownLeft
    hasEverBeenCompleteHatch ||= is(Zerg.Hatchery) && complete
  }
  
  private lazy val stuckMoveFrames    = 10
  private lazy val stuckAttackFrames  = Math.max(stuckMoveFrames, cooldownMaxAirGround)
  def seeminglyStuck: Boolean = framesFailingToMove > stuckMoveFrames || framesFailingToAttack > stuckAttackFrames
  
  ////////////
  // Health //
  ////////////
  
  def aliveAndComplete:Boolean = alive && complete
  
  def energyMax     : Int = unitClass.maxEnergy //TODO: Add upgrades
  def mineralsLeft  : Int = if (unitClass.isMinerals) resourcesLeft else 0
  def gasLeft       : Int = if (unitClass.isGas)      resourcesLeft else 0
  
  ///////////////
  // Economics //
  ///////////////

  // When a Larva is about to morph, but hasn't turned into an egg, remainingCompletionFrames is ZERO
  def completeOrNearlyComplete: Boolean = complete || (remainingCompletionFrames < With.latency.framesRemaining && ( ! isAny(Zerg.Larva, Zerg.Hydralisk, Zerg.Mutalisk)))
  
  lazy val isBlocker: Boolean = gasLeft + mineralsLeft < With.configuration.blockerMineralThreshold
  
  def subjectiveValue: Double = subjectiveValueCache()
  private val subjectiveValueCache = new Cache(() =>
    unitClass.subjectiveValue
      + scarabCount * Protoss.Scarab.subjectiveValue
      + interceptorCount * Protoss.Interceptor.subjectiveValue
      + (if (unitClass.isTransport) friendly.map(_.loadedUnits.map(_.subjectiveValue).sum).sum else 0))

  def remainingOccupationFrames: Int = Vector(
    remainingCompletionFrames,
    remainingTechFrames,
    remainingUpgradeFrames,
    remainingTrainFrames,
    addon.map(_.remainingCompletionFrames).getOrElse(0)
  ).max

  val participatingInCombat = new Cache(() => matchups.targets.nonEmpty || isAny(
    // Spellcasters (which don't have targets)
    // Static defense (which doesn't have targets if incomplete)
    Terran.Dropship,
    Terran.Medic,
    Terran.ScienceVessel,
    Terran.SpiderMine,
    Terran.MissileTurret,
    Terran.Bunker,
    Protoss.DarkArchon,
    Protoss.HighTemplar,
    Protoss.Shuttle,
    Protoss.ShieldBattery,
    Zerg.CreepColony,
    Zerg.Defiler,
    Zerg.SporeColony,
    Zerg.SunkenColony
  ))
  val baseTargetValue = new Cache(() => EvaluateTargets.getTargetBaseValue(this))
  
  //////////////
  // Geometry //
  //////////////
  
  def x: Int = pixelCenter.x
  def y: Int = pixelCenter.y
  
  def left    : Int = x - unitClass.dimensionLeft
  def right   : Int = x + unitClass.dimensionRight
  def top     : Int = y - unitClass.dimensionUp
  def bottom  : Int = y + unitClass.dimensionDown
  
  def topLeft     : Pixel = Pixel(left, top)
  def topRight    : Pixel = Pixel(right, top)
  def bottomLeft  : Pixel = Pixel(left, bottom)
  def bottomRight : Pixel = Pixel(right, bottom)
  def corners: Vector[Pixel] = Vector(topLeft, topRight, bottomLeft, bottomRight)
  
  def tileIncludingCenter:  Tile          = pixelCenter.tileIncluding
  def tiles:                Seq[Tile]     = cacheTiles()
  def tileArea:             TileRectangle = cacheTileArea()
  def addonArea:            TileRectangle = TileRectangle(Tile(0, 0), Tile(2, 2)).add(tileTopLeft).add(4,1)

  private def tileCacheDuration: Int = { if (unitClass.isBuilding) (if (unitClass.canFly) 24 * 5 else 24 * 60) else 1 }
  private lazy val cacheTileArea = new Cache(() => unitClass.tileArea.add(tileTopLeft), refreshPeriod = tileCacheDuration)
  private lazy val cacheTiles = new Cache(() => cacheTileArea().tiles.toVector, refreshPeriod = tileCacheDuration)
  
  def zone: Zone = cacheZone()
  // Hack to get buildings categorized in zone they were intended to be constructed in
  private val cacheZone = new Cache(() => if (unitClass.isBuilding) topLeft.zone else pixelCenter.zone)
  
  def base: Option[Base] = cacheBase()
  private val cacheBase = new Cache(() => pixelCenter.base)
  
  def mobilityForceGrid : AbstractGrid[Force]   = if (flying) With.grids.mobilityForceAir else With.grids.mobilityForceGround
  def mobilityGrid      : AbstractGrid[Int]     = if (flying) With.grids.mobilityAir else With.grids.mobilityGround
  def mobilityForce     : Force                 = mobilityForceGrid.get(tileIncludingCenter)
  def mobility          : Int                   = mobilityGrid.get(tileIncludingCenter)
  
  def pixelRangeMin: Double = unitClass.groundMinRangeRaw
  def pixelRangeAir: Double = pixelRangeAirCache()
  private val pixelRangeAirCache = new Cache(() =>
    unitClass.pixelRangeAir +
      (if (isBunker())                                                  32.0 else 0.0) +
      (if (isBunker()     && player.hasUpgrade(Terran.MarineRange))     32.0 else 0.0) +
      (if (isMarine()     && player.hasUpgrade(Terran.MarineRange))     32.0 else 0.0) +
      (if (isGoliath()    && player.hasUpgrade(Terran.GoliathAirRange)) 96.0 else 0.0) +
      (if (isDragoon()    && player.hasUpgrade(Protoss.DragoonRange))   64.0 else 0.0) +
      (if (isHydralisk()  && player.hasUpgrade(Zerg.HydraliskRange))    32.0 else 0.0),
    24) // Cache to avoid a bunch of IS calls
  
  def pixelRangeGround: Double = pixelRangeGroundCache()
  private val pixelRangeGroundCache = new Cache(() =>
    unitClass.pixelRangeGround +
      (if (isBunker())                                                32.0 else 0.0) +
      (if (isBunker()     && player.hasUpgrade(Terran.MarineRange))   32.0 else 0.0) +
      (if (isMarine()     && player.hasUpgrade(Terran.MarineRange))   32.0 else 0.0) +
      (if (isDragoon()    && player.hasUpgrade(Protoss.DragoonRange)) 64.0 else 0.0) +
      (if (isHydralisk()  && player.hasUpgrade(Zerg.HydraliskRange))  32.0 else 0.0),
    24)  // Cache to avoid a bunch of IS calls
  
  def pixelRangeMax: Double = Math.max(pixelRangeAir, pixelRangeGround)
  
  def canTraverse(tile: Tile): Boolean = flying || With.grids.walkable.get(tile)
  
  def pixelStart                                                : Pixel   = Pixel(left, top)
  def pixelEnd                                                  : Pixel   = Pixel(right, bottom)
  def pixelStartAt            (at: Pixel)                       : Pixel   = at.subtract(pixelCenter).add(left, top)
  def pixelEndAt              (at: Pixel)                       : Pixel   = at.subtract(pixelCenter).add(right, bottom)
  def pixelDistanceCenter     (otherPixel:  Pixel)              : Double  = pixelCenter.pixelDistance(otherPixel)
  def pixelDistanceCenter     (otherUnit:   UnitInfo)           : Double  = pixelDistanceCenter(otherUnit.pixelCenter)
  def pixelDistanceShooting   (other:       UnitInfo)           : Double  = if (unitClass.isReaver && zone != other.zone) pixelDistanceTravelling(other.pixelCenter) else pixelDistanceEdge(other.pixelStart, other.pixelEnd)
  def pixelDistanceEdge       (other:       UnitInfo)           : Double  = pixelDistanceEdge(other.pixelStart, other.pixelEnd)
  def pixelDistanceEdge       (other: UnitInfo, otherAt: Pixel) : Double  = pixelDistanceEdge(otherAt.subtract(other.unitClass.dimensionLeft, other.unitClass.dimensionUp), otherAt.add(other.unitClass.dimensionRight, other.unitClass.dimensionDown))
  def pixelDistanceEdge       (oStart: Pixel, oEnd: Pixel)      : Double  = PurpleMath.broodWarDistanceBox(pixelStart, pixelEnd, oStart, oEnd)
  def pixelDistanceEdge       (destination: Pixel)              : Double  = PurpleMath.broodWarDistanceBox(pixelStart, pixelEnd, destination, destination)
  def pixelDistanceSquared    (otherUnit:   UnitInfo)           : Double  = pixelDistanceSquared(otherUnit.pixelCenter)
  def pixelDistanceSquared    (otherPixel:  Pixel)              : Double  = pixelCenter.pixelDistanceSquared(otherPixel)
  def pixelDistanceTravelling (destination: Pixel)              : Double  = pixelDistanceTravelling(pixelCenter, destination)
  def pixelDistanceTravelling (destination: Tile)               : Double  = pixelDistanceTravelling(pixelCenter, destination.pixelCenter)
  def pixelDistanceTravelling (from: Pixel, to: Pixel)          : Double  = if (flying) from.pixelDistance(to) else from.nearestWalkableTerrain.groundPixels(to.nearestWalkableTerrain)

  def velocity: Force = Force(velocityX, velocityY)

  def canMove: Boolean = canMoveCache()
  private val canMoveCache = new Cache(() =>
    (unitClass.canMove || (unitClass.isBuilding && flying))
    && unitClass.topSpeed > 0
    && canDoAnything
    && ! burrowed
    && ! sieged)

  def topSpeed: Double = if (canMove) topSpeedPossibleCache() else 0
  def topSpeedPossible: Double = topSpeedPossibleCache()
  private val topSpeedPossibleCache = new Cache(() =>
    (if (ensnared) 0.5 else 1.0) * // TODO: Is this the multiplier?
    (if (stimmed) 1.5 else 1.0) * (
    (if (isSiegeTankSieged()) Terran.SiegeTankUnsieged.topSpeed else unitClass.topSpeed)
    * (if (
      (isVulture()    && player.hasUpgrade(Terran.VultureSpeed))    ||
      (isObserver()   && player.hasUpgrade(Protoss.ObserverSpeed))  ||
      (isScout()      && player.hasUpgrade(Protoss.ScoutSpeed))     ||
      (isShuttle()    && player.hasUpgrade(Protoss.ShuttleSpeed))   ||
      (isZealot()     && player.hasUpgrade(Protoss.ZealotSpeed))    ||
      (isOverlord()   && player.hasUpgrade(Zerg.ZerglingSpeed))     ||
      (isHydralisk()  && player.hasUpgrade(Zerg.HydraliskSpeed))    ||
      (isUltralisk()  && player.hasUpgrade(Zerg.UltraliskSpeed)))
      1.5 else 1.0)))

  def projectFrames(framesToLookAhead: Double): Pixel = pixelCenter.add((velocityX * framesToLookAhead).toInt, (velocityY * framesToLookAhead).toInt)

  def inTileRadius  (tiles: Int)  : Traversable[UnitInfo] = With.units.inTileRadius(tileIncludingCenter, tiles)
  def inPixelRadius (pixels: Int) : Traversable[UnitInfo] = With.units.inPixelRadius(pixelCenter, pixels)

  def isTransport: Boolean = unitClass.isTransport && ( ! isOverlord() || player.hasUpgrade(Zerg.OverlordDrops))

  def detectionRangePixels: Int = if (unitClass.isDetector) (if (unitClass.isBuilding) 32 * 7 else sightRangePixels) else 0
  def sightRangePixels: Int = sightRangePixelsCache()
  private val sightRangePixelsCache = new Cache(() =>
    if (blind) 32 else
    unitClass.sightRangePixels +
      (if (
        (isGhost()    && player.hasUpgrade(Terran.GhostVisionRange))      ||
        (isObserver() && player.hasUpgrade(Protoss.ObserverVisionRange))  ||
        (isScout()    && player.hasUpgrade(Protoss.ScoutVisionRange))     ||
        (isOverlord() && player.hasUpgrade(Zerg.OverlordVisionRange)))
      64 else 0))

  def altitudeBonus: Double = tileIncludingCenter.altitudeBonus

  val arrivalFrame = new Cache(() => {
    val home        = With.geography.home.pixelCenter
    val classSpeed  = unitClass.topSpeed
    val travelTime  = Math.min(24 * 60 * 60,
      if (canMove)
        framesToTravelTo(home)
      else if (classSpeed > 0)
        (pixelDistanceTravelling(home) / classSpeed).toInt
      else
        Int.MaxValue)
    val completionTime  = PurpleMath.clamp(completionFrame, With.frame, With.frame + unitClass.buildFrames)
    val arrivalTime     = completionTime + travelTime
    arrivalTime
  })

  ////////////
  // Combat //
  ////////////

  def battle: Option[BattleLocal] = With.battles.byUnit.get(this).orElse(With.matchups.entrants.find(_._2.contains(this)).map(_._1))
  def matchups: MatchupAnalysis = With.matchups.get(this)

  def armorHealth: Int = armorHealthCache()
  def armorShield: Int = armorShieldsCache()

  lazy val armorHealthCache   = new Cache(() => unitClass.armor + unitClass.armorUpgrade.map(player.getUpgradeLevel).getOrElse(0))
  lazy val armorShieldsCache  = new Cache(() => player.getUpgradeLevel(Protoss.Shields))

  def totalHealth: Int = hitPoints + shieldPoints + defensiveMatrixPoints

  def stimAttackSpeedBonus: Int = if (stimmed) 2 else 1

  def dpfAir    : Double = damageOnHitAir     * attacksAgainstAir     / cooldownMaxAir.toDouble
  def dpfGround : Double = damageOnHitGround  * attacksAgainstGround  / cooldownMaxGround.toDouble

  def attacksAgainstAir: Int = attacksAgainstAirCache()
  private val attacksAgainstAirCache = new Cache(() => {
    var output = unitClass.airDamageFactorRaw * unitClass.maxAirHitsRaw
    if (output == 0  && isBunker())    output = 4
    if (output == 0  && isCarrier())  output = interceptorCount
    output
  })

  def attacksAgainstGround: Int = attacksAgainstGroundCache()
  private val attacksAgainstGroundCache = new Cache(() => {
    var output = unitClass.groundDamageFactorRaw * unitClass.maxGroundHitsRaw
    if (output == 0  && isBunker())   output = 4
    if (output == 0  && isCarrier())  output = interceptorCount
    if (output == 0  && isReaver())   output = 1
    output
  })

  //TODO: Ensnare
  def cooldownLeft      : Int = if (complete) Math.max(Math.max(airCooldownLeft, groundCooldownLeft), if (friendly.exists(_.transport.exists(_.flying))) cooldownMaxAirGround / 2 else 0) else remainingCompletionFrames
  def cooldownMaxAir    : Int = (2 + unitClass.airDamageCooldown)     / stimAttackSpeedBonus // +2 is the RNG
  def cooldownMaxGround : Int = (2 + unitClass.groundDamageCooldown)  / stimAttackSpeedBonus // +2 is the RNG

  def cooldownMaxAirGround: Int = Math.max(
    if (unitClass.attacksAir)     cooldownMaxAir    else 0,
    if (unitClass.attacksGround)  cooldownMaxGround else 0)

  def cooldownMaxAgainst(enemy: UnitInfo): Int = if (enemy.flying) cooldownMaxAir else cooldownMaxGround

  @inline
  final def pixelRangeAgainst(enemy: UnitInfo): Double = if (enemy.flying) pixelRangeAir else pixelRangeGround
  def effectiveRangePixels: Double = Math.max(pixelRangeMax, unitClass.effectiveRangePixels)

  def hitChanceAgainst(
    enemy : UnitInfo,
    from  : Option[Pixel] = None,
    to    : Option[Pixel] = None)
  : Double = {
    if (guaranteedToHit(enemy, from, to)) 1.0 else 0.47
  }

  def guaranteedToHit(
    enemy : UnitInfo,
    from  : Option[Pixel] = None,
    to    : Option[Pixel] = None)
  : Boolean = {
    val tileFrom  = from.getOrElse(pixelCenter).tileIncluding
    val tileTo    =   to.getOrElse(pixelCenter).tileIncluding
    (
      flying
      || enemy.flying
      || unitClass.isReaver
      || unitClass.unaffectedByDarkSwarm
      || With.grids.altitudeBonus.get(tileFrom) >= With.grids.altitudeBonus.get(tileTo)
    )
  }
  
  def damageTypeAgainst (enemy: UnitInfo)  : Damage.Type  = if (enemy.flying) unitClass.airDamageType    else unitClass.groundDamageType
  def attacksAgainst    (enemy: UnitInfo)  : Int          = if (enemy.flying) attacksAgainstAir          else attacksAgainstGround
  
  def damageScaleAgainstHitPoints(enemy: UnitInfo): Double = {
    if (dpfAir    <= 0 && enemy.flying)   return 0.0
    if (dpfGround <= 0&& ! enemy.flying)  return 0.0
    Damage.scaleBySize(damageTypeAgainst(enemy), enemy.unitClass.size)
  }
  
  def damageUpgradeLevel  : Int = unitClass.damageUpgradeType.map(player.getUpgradeLevel).getOrElse(0)
  def damageOnHitGround   : Int = damageOnHitGroundCache()
  def damageOnHitAir      : Int = damageOnHitAirCache()
  def damageOnHitMax      : Int = Math.max(damageOnHitAir, damageOnHitGround)
  private val damageOnHitGroundCache  = new Cache(() => unitClass.effectiveGroundDamage  + unitClass.groundDamageBonusRaw  * damageUpgradeLevel)
  private val damageOnHitAirCache     = new Cache(() => unitClass.effectiveAirDamage     + unitClass.airDamageBonusRaw     * damageUpgradeLevel)
  
  def damageOnHitBeforeShieldsArmorAndDamageType(enemy: UnitInfo): Int = if(enemy.flying) damageOnHitAir else damageOnHitGround
  def damageOnNextHitAgainst(
    enemy   : UnitInfo,
    shields : Option[Int] = None,
    from    : Option[Pixel] = None,
    to      : Option[Pixel] = None)
      : Int = {
    val enemyShieldPoints       = shields.getOrElse(enemy.shieldPoints)
    val hits                    = attacksAgainst(enemy)
    val damagePerHit            = damageOnHitBeforeShieldsArmorAndDamageType(enemy: UnitInfo)
    val damageAssignedTotal     = hits * damagePerHit
    val damageAssignedToShields = Math.min(damageAssignedTotal, enemyShieldPoints + enemy.armorShield * hits)
    val damageToShields         = damageAssignedToShields - enemy.armorShield * hits
    val damageAssignedToHealth  = damageAssignedTotal - damageAssignedToShields
    val damageToHealthScale     = damageScaleAgainstHitPoints(enemy)
    val damageToHealth          = Math.max(0.0, (damageAssignedToHealth - enemy.armorHealth * hits) * damageToHealthScale)
    val damageDealtTotal        = damageToHealth + damageToShields
    val hitChance               = hitChanceAgainst(enemy, from, to)
    val output                  = (hitChance * Math.max(1.0, damageDealtTotal)).toInt
    output
  }
  
  def dpfOnNextHitAgainst(enemy: UnitInfo): Double = {
    if (unitClass.suicides) {
      damageOnNextHitAgainst(enemy)
    }
    else {
      val cooldownVs = cooldownMaxAgainst(enemy)
      if (cooldownVs == 0)
        0.0
      else
        damageOnNextHitAgainst(enemy).toDouble / cooldownVs
    }
  }
  def vpfOnNextHitAgainst(enemy: UnitInfo): Double = dpfOnNextHitAgainst(enemy) / enemy.subjectiveValue
  
  def canDoAnything: Boolean = canDoAnythingCache()
  private val canDoAnythingCache = new Cache(() =>
    aliveAndComplete
    && ( ! unitClass.requiresPsi || powered)
    && ! stasised
    && ! maelstrommed
    && ! lockedDown)
  
  def canBeAttacked: Boolean = canBeAttackedCache()
  private val canBeAttackedCache = new Cache(() =>
      alive &&
      (complete || unitClass.isBuilding) &&
      totalHealth > 0 &&
      ! invincible &&
      ! stasised)
  
  def canAttack: Boolean = canAttackCache()
  private val canAttackCache = new Cache(() =>
    canDoAnything
    && ( ! unitClass.shootsScarabs || scarabCount > 0)
    && (
      unitClass.rawCanAttack
      || isBunker()
      || (isCarrier() && interceptorCount > 0)
      || (isReaver()  && scarabCount > 0)
      || (isLurker()  && burrowed))
    && (flying || ! underDisruptionWeb))
  
  def canAttack(enemy: UnitInfo): Boolean = (
    canAttack
    && enemy.canBeAttacked
    && (if (enemy.flying) unitClass.attacksAir else unitClass.attacksGround)
    && ! enemy.effectivelyCloaked
    && (enemy.unitClass.triggersSpiderMines || ! isSpiderMine())
    && (unitClass.unaffectedByDarkSwarm || ! enemy.underDarkSwarm)
  )

  def canBurrow: Boolean = canDoAnything && (is(Zerg.Lurker) || (player.hasTech(Zerg.Burrow) && isAny(Zerg.Drone, Zerg.Zergling, Zerg.Hydralisk, Zerg.Defiler)))
  
  // Stupid, but helps performance due to costliness of comparing unit classes with BWMirror limitations
  protected class CacheIs(unitClass: UnitClass) extends Cache(() => is(unitClass))
  lazy val isSpiderMine         : CacheIs = new CacheIs(Terran.SpiderMine)
  lazy val isBunker             : CacheIs = new CacheIs(Terran.Bunker)
  lazy val isCommandCenter      : CacheIs = new CacheIs(Terran.CommandCenter)
  lazy val isMarine             : CacheIs = new CacheIs(Terran.Marine)
  lazy val isGhost              : CacheIs = new CacheIs(Terran.Ghost)
  lazy val isVulture            : CacheIs = new CacheIs(Terran.Vulture)
  lazy val isGoliath            : CacheIs = new CacheIs(Terran.Goliath)
  lazy val isWraith             : CacheIs = new CacheIs(Terran.Wraith)
  lazy val isSiegeTankSieged    : CacheIs = new CacheIs(Terran.SiegeTankSieged)
  lazy val isSiegeTankUnsieged  : CacheIs = new CacheIs(Terran.SiegeTankUnsieged)
  lazy val isComsat             : CacheIs = new CacheIs(Terran.Comsat)
  lazy val isControlTower       : CacheIs = new CacheIs(Terran.ControlTower)
  lazy val isEngineeringBay     : CacheIs = new CacheIs(Terran.EngineeringBay)
  lazy val isAcademy            : CacheIs = new CacheIs(Terran.Academy)
  lazy val isScienceFacility    : CacheIs = new CacheIs(Terran.ScienceFacility)
  lazy val isScannerSweep       : CacheIs = new CacheIs(Terran.SpellScannerSweep)

  lazy val isZealot             : CacheIs = new CacheIs(Protoss.Zealot)
  lazy val isDragoon            : CacheIs = new CacheIs(Protoss.Dragoon)
  lazy val isObserver           : CacheIs = new CacheIs(Protoss.Observer)
  lazy val isShuttle            : CacheIs = new CacheIs(Protoss.Shuttle)
  lazy val isScout              : CacheIs = new CacheIs(Protoss.Scout)
  lazy val isCarrier            : CacheIs = new CacheIs(Protoss.Carrier)
  lazy val isInterceptor        : CacheIs = new CacheIs(Protoss.Interceptor)
  lazy val isReaver             : CacheIs = new CacheIs(Protoss.Reaver)
  lazy val isArbiter            : CacheIs = new CacheIs(Protoss.Arbiter)
  lazy val isForge              : CacheIs = new CacheIs(Protoss.Forge)
  lazy val isRoboticsFacility   : CacheIs = new CacheIs(Protoss.RoboticsFacility)
  lazy val isObservatory        : CacheIs = new CacheIs(Protoss.Observatory)
  lazy val isOverlord           : CacheIs = new CacheIs(Zerg.Overlord)
  lazy val isHydralisk          : CacheIs = new CacheIs(Zerg.Hydralisk)
  lazy val isMutalisk           : CacheIs = new CacheIs(Zerg.Mutalisk)
  lazy val isLurker             : CacheIs = new CacheIs(Zerg.Lurker)
  lazy val isUltralisk          : CacheIs = new CacheIs(Zerg.Ultralisk)
  lazy val isGuardian           : CacheIs = new CacheIs(Zerg.Guardian)

  
  // Frame X:     Unit's cooldown is 0.   Unit starts attacking.
  // Frame X-1:   Unit's cooldown is 1.   Unit receives attack order.
  // Frame X-1-L: Unit's cooldown is L+1. Send attack order.
  def framesToBeReadyForAttackOrder: Int = cooldownLeft - With.latency.framesRemaining - With.reaction.agencyMin
  def readyForAttackOrder: Boolean = canAttack && framesToBeReadyForAttackOrder <= 0
  
  def pixelsTravelledMax(framesAhead: Int): Double = if (canMove) topSpeed * framesAhead else 0.0
  def pixelReachAir     (framesAhead: Int): Double = pixelsTravelledMax(framesAhead) + pixelRangeAir
  def pixelReachGround  (framesAhead: Int): Double = pixelsTravelledMax(framesAhead) + pixelRangeGround
  def pixelReachMax     (framesAhead: Int): Double = Math.max(pixelReachAir(framesAhead), pixelReachGround(framesAhead))
  def pixelReachAgainst (framesAhead: Int, enemy: UnitInfo): Double = if (enemy.flying) pixelReachAir(framesAhead) else pixelReachGround(framesAhead)
  def pixelToFireAt(enemy: UnitInfo): Pixel = enemy.pixelCenter.project(pixelCenter, Math.min(pixelDistanceEdge(enemy), pixelRangeAgainst(enemy) + unitClass.radialHypotenuse + enemy.unitClass.radialHypotenuse))
  def inRangeToAttack(enemy: UnitInfo)                    : Boolean = pixelDistanceEdge(enemy)          <= pixelRangeAgainst(enemy) && (pixelRangeMin <= 0.0 || pixelDistanceEdge(enemy)          > pixelRangeMin)
  def inRangeToAttack(enemy: UnitInfo, enemyAt: Pixel)    : Boolean = pixelDistanceEdge(enemy, enemyAt) <= pixelRangeAgainst(enemy) && (pixelRangeMin <= 0.0 || pixelDistanceEdge(enemy, enemyAt) > pixelRangeMin)
  def inRangeToAttack(enemy: UnitInfo, framesAhead: Int)  : Boolean = inRangeToAttack(enemy, enemy.projectFrames(framesAhead))
  def pixelsToGetInRange(enemy: UnitInfo)                 : Double = if (canAttack(enemy)) (pixelDistanceEdge(enemy) - pixelRangeAgainst(enemy)) else LightYear()
  def pixelsToGetInRange(enemy: UnitInfo, enemyAt: Pixel) : Double = if (canAttack(enemy)) (pixelDistanceEdge(enemy, enemyAt) - pixelRangeAgainst(enemy)) else LightYear()
  def framesToTravelTo(destination: Pixel)  : Int = framesToTravelPixels(pixelDistanceTravelling(destination))
  def framesToTravelPixels(pixels: Double)  : Int = (if (pixels <= 0.0) 0 else if (canMove) Math.max(0, Math.ceil(pixels / topSpeedPossible).toInt) else Forever()) + (if (burrowed || sieged) 24 else 0)
  def framesToTurnTo(radiansTo: Double): Double = unitClass.framesToTurn(PurpleMath.normalizeAroundZero(PurpleMath.radiansTo(angleRadians, radiansTo)))
  def framesToTurnFrom(enemy: UnitInfo): Double = framesToTurnTo(enemy.pixelCenter.radiansTo(pixelCenter))
  def framesToStopRightNow: Double = if (unitClass.isFlyer || unitClass.floats) PurpleMath.clamp(PurpleMath.nanToZero(framesToAccelerate * speed / topSpeed), 0.0, framesToAccelerate) else 0.0
  def framesToAccelerate: Double = PurpleMath.clamp(PurpleMath.nanToZero((topSpeed - speed) / unitClass.accelerationFrames), 0, unitClass.accelerationFrames)
  def framesToGetInRange(enemy: UnitInfo)                 : Int = if (canAttack(enemy)) framesToTravelPixels(pixelsToGetInRange(enemy)) else Forever()
  def framesToGetInRange(enemy: UnitInfo, enemyAt: Pixel) : Int = if (canAttack(enemy)) framesToTravelPixels(pixelsToGetInRange(enemy, enemyAt)) else Forever()
  def framesBeforeAttacking(enemy: UnitInfo)              : Int = framesBeforeAttacking(enemy, enemy.pixelCenter)
  def framesBeforeAttacking(enemy: UnitInfo, at: Pixel)   : Int = {
    if (canAttack(enemy)) {
      Math.max(cooldownLeft, framesToGetInRange(enemy))
    }
    else Forever()
  }
  
  def canStim: Boolean = unitClass.canStim && player.hasTech(Terran.Stim) && hitPoints > 10

  def moving: Boolean = velocityX != 0 || velocityY != 0

  def speed: Double = velocity.lengthFast
  def speedApproaching(other: UnitInfo): Double = speedApproaching(other.pixelCenter)
  def speedApproaching(pixel: Pixel): Double = {
    val deltaXY = Force(x - pixel.x, y - pixel.y)
    val deltaV  = velocity
    val output  = - velocity.lengthFast * (deltaXY.normalize * velocity.normalize)
    output
  }
  def speedApproachingEachOther(other: UnitInfo): Double = speedApproaching(other) + other.speedApproaching(this)
  
  ////////////
  // Orders //
  ////////////
  
  def gathering: Boolean = gatheringMinerals || gatheringGas
  
  def carryingResources: Boolean = carryingMinerals || carryingGas

  def presumptiveTarget: Option[UnitInfo] =
    target
      .orElse(orderTarget)
      .orElse(orderTargetPixel.flatMap(somePixel => ByOption.minBy(matchups.targets)(_.pixelDistanceEdge(somePixel))))

  def isBeingViolent: Boolean = {
    unitClass.isStaticDefense ||
    attacking                 ||
    cooldownLeft > 0          ||
    target.exists(isEnemyOf)
  }
  
  def isBeingViolentTo(victim: UnitInfo): Boolean = {
    isBeingViolent &&
    //Are we capable of hurting the victim?
    isEnemyOf(victim) &&
    canAttack(victim) &&
    //Are we not attacking anyone else?
    ! target.exists(_ != victim) &&
    //Are we close to being able to hit the victim?
    framesToGetInRange(victim) < With.configuration.violenceThresholdFrames
  }

  def techProducing: Option[Tech]
  def upgradeProducing: Option[Upgrade]
  def unitProducing: Option[UnitClass]
  
  ////////////////
  // Visibility //
  ////////////////
  
  def visibleToOpponents: Boolean =
    if (isFriendly)
      With.grids.enemyVision.isSet(tileIncludingCenter) || With.framesSince(lastFrameTakingDamage) < GameTime(0, 2)() || With.framesSince(lastFrameStartingAttack) < GameTime(0, 2)()
    else
      visible
  
  @inline def likelyStillThere: Boolean = cacheLikelyStillThere()
  private val cacheLikelyStillThere = new Cache(() => possiblyStillThere &&
    ( ! canMove
      || With.framesSince(lastSeen) < With.configuration.fogPositionDurationFrames
      || is(UnitMatchSiegeTank)
      || (player.isTerran && base.exists(_.owner == player))))
  
  @inline def likelyStillAlive: Boolean = (
    likelyStillThere
    || unitClass.isBuilding
    || unitClass.isWorker
    || With.framesSince(lastSeen) < (
      if (With.strategy.isFfa)
        GameTime(2, 0)()
      else
        GameTime(5, 0)()
    )
  )

  def cloakedOrBurrowed: Boolean = cloaked || burrowed
  def effectivelyCloaked: Boolean =
    (burrowed || cloaked) &&
    ( ! ensnared && ! plagued) && (
      if (isFriendly) ! With.grids.enemyDetection.isDetected(tileIncludingCenter)
      else ! detected
    )
  
  /////////////
  // Players //
  /////////////
  
  def isOurs     : Boolean = player.isUs
  def isNeutral  : Boolean = player.isNeutral
  def isFriendly : Boolean = player.isAlly || isOurs
  def isEnemy    : Boolean = player.isEnemy
  def isEnemyOf(otherUnit: UnitInfo): Boolean = (isFriendly && otherUnit.isEnemy) || (isEnemy && otherUnit.isFriendly)
  def isAllyOf(otherUnit: UnitInfo): Boolean = (isFriendly && otherUnit.isFriendly) || (isEnemy && otherUnit.isEnemy)
  
  ///////////////////
  // Visualization //
  ///////////////////
  
  def color: Color =
    if      (visible)             player.colorBright
    else if (likelyStillThere)    player.colorMedium
    else if (possiblyStillThere)  player.colorDark
    else if (likelyStillAlive)    player.colorMidnight
    else                          Colors.MidnightGray
}
