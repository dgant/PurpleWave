package Information.Battles.Prediction.Simulation

import Debugging.Visualizations.Views.Battles.ShowBattle
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Simulacrum(
  val simulation  : Simulation,
  val realUnit    : UnitInfo) {
  
  // Constant
  private val SIMULATION_STEP_FRAMES = 6

  ////////////////
  // Unit state ///////////////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////

  val unitClass   : UnitClass   = realUnit.unitClass
  val player      : PlayerInfo  = realUnit.player
  val isEnemy     : Boolean     = realUnit.isEnemy
  val isFriendly  : Boolean     = realUnit.isFriendly
  val flying      : Boolean     = realUnit.flying
  val value       : Double      = realUnit.subjectiveValue

  val speedMultiplier   : Double  = if (isEnemy || flying) 1.0 else Math.max(0.75, simulation.chokeMobility.getOrElse(realUnit.zone, 1.0))
  val bonusRange        : Double  = if (isFriendly || ! unitClass.isSiegeTank || ! simulation.weAttack) 0.0 else With.configuration.simulationBonusTankRange
  val multiplierSplash  : Double  = realUnit.matchups.splashFactorMax

  val canMove             : Boolean             = realUnit.canMove
  var topSpeed            : Double              = realUnit.topSpeed * speedMultiplier
  var shieldPointsInitial : Int                 = realUnit.shieldPoints + realUnit.defensiveMatrixPoints
  var shieldPoints        : Int                 = shieldPointsInitial
  val hitPointsInitial    : Int                 = realUnit.hitPoints
  var hitPoints           : Int                 = hitPointsInitial
  var cooldownShooting    : Int                 = Math.max(realUnit.remainingCompletionFrames, 1 + realUnit.cooldownLeft)
  var cooldownMoving      : Int                 = Math.max(realUnit.remainingCompletionFrames, 0)
  val pixelRangeMin       : Double              = realUnit.pixelRangeMin
  val pixelInitial        : Pixel               = realUnit.pixelCenter
  var pixel               : Pixel               = pixelInitial
  var dead                : Boolean             = false
  var target              : Option[Simulacrum]  = None
  var pathBendiness       : Double              = 1.0
  val fleePixel           : Pixel               = simulation.focus.project(pixel, 10000).clamp
  
  // Scorekeeping
  val valuePerDamage    : Double                = PurpleMath.nanToZero(realUnit.subjectiveValue / unitClass.maxTotalHealth)
  var damageDealt       : Double                = 0.0
  var damageReceived    : Double                = 0.0
  var valueDealt        : Double                = 0.0
  var valueReceived     : Double                = 0.0
  var kills             : Int                   = 0
  var events: ArrayBuffer[SimulationEvent] = new ArrayBuffer[SimulationEvent]

  val realTargets: Seq[UnitInfo] = realUnit.matchups.targets
    .filter(target =>
      ! simulation.fleeing
      || realUnit.inRangeToAttack(target)
      || ! realUnit.isOurs)
  lazy val targetQueue: ArrayBuffer[Simulacrum] = new mutable.ArrayBuffer[Simulacrum] ++ realTargets.flatMap(simulation.simulacra.get)
  
  var fightingInitially: Boolean = {
    if ( ! realUnit.canAttack) {
      false
    }
    else if ( ! realUnit.unitClass.dealsDamage) {
      false
    }
    else if (realUnit.unitClass.isWorker) {
      realUnit.attacking || realUnit.friendly.exists(_.squad.isDefined)
    }
    else if (realUnit.isEnemy) {
      true
    }
    else {
      realUnit.canMove || realUnit.matchups.targetsInRange.nonEmpty
    }
  }
  var fighting: Boolean = fightingInitially
    
  if (realUnit.is(Zerg.Lurker) && ! realUnit.burrowed) {
    cooldownShooting = 36
    topSpeed = 0
    true
  }

  //////////////////////
  // Simulation steps /////////////////////////////////////////////////////////////////////////////////////////////////
  //////////////////////

  def updateDeath() {
    dead = dead || hitPoints <= 0
    if (dead) {
      hitPoints = 0
      shieldPoints = 0
    }
  }
  
  def step() {
    if (dead) return
    if (simulation.fleeing && isFriendly) fighting = false
    cooldownMoving    -= 1
    cooldownShooting  -= 1
    tryToAcquireTarget()
    tryToChaseTarget()
    tryToStrikeTarget()
    tryToRunAway()
    if (cooldownShooting > 0)           simulation.updated = true
    if (cooldownMoving > 0 && fighting) simulation.updated = true
  }

  // It'd be nice if this lined up with real targeting logic
  @inline final def targetValue(target: Simulacrum): Double = {
    val distance = Math.max(0.0, target.pixel.pixelDistance(pixel) - realUnit.pixelRangeAgainst(target.realUnit))
    val targetValue = target.realUnit.baseTargetValue()
    targetValue - 1000000 * distance
  }
  
  def tryToAcquireTarget() {
    if ( ! fighting)                                return
    if (cooldownMoving > 0 || cooldownShooting > 0) return
    if (validTargetExistsInRange)                   return
  
    val lastTarget = target
    target.foreach(targetQueue.+=)
    target = None
    while (target.isEmpty && targetQueue.nonEmpty) {
      val candidateIndex = targetQueue.indices.maxBy(i => targetValue(targetQueue(i)))
      val candidate = targetQueue(candidateIndex)
      targetQueue.remove(candidateIndex)
      if (isValidTarget(candidate)) {
        target = Some(candidate)
      }
    }
    if (target.isEmpty) {
      fighting = false
    }
    else if (target != lastTarget) {
      // TODO: Stop using real unit positions!
      val distanceByAir     : Double = realUnit.pixelDistanceEdge(target.get.realUnit)
      val distanceTraveling : Double = realUnit.pixelDistanceTravelling(target.get.realUnit.pixelCenter)
      pathBendiness = PurpleMath.clamp(PurpleMath.nanToInfinity(distanceByAir / distanceTraveling), 1.0, 3.0)
    }
  }
  
  def tryToChaseTarget() {
    if ( ! fighting)              return
    if (validTargetExistsInRange) return
    if ( ! validTargetExists)     return
    moveTowards(target.get.pixel)
  }
  
  def tryToStrikeTarget() {
    if ( ! fighting)                  return
    if (cooldownShooting > 0)         return
    if ( ! validTargetExistsInRange)  return
    dealDamage(target.get)
  }
  
  def tryToRunAway() {
    if (fighting) return
    // Assume units at home are trapped
    // Prevents us from thinking workers can easily escape
    if (pixel.base.exists(_.owner == player)) return
    pathBendiness = 1.0
    moveTowards(fleePixel)
  }
  
  private def dealDamage(victim: Simulacrum) {
    val splashFactor      = if (multiplierSplash == 1.0) 1.0 else Math.max(1.0, Math.min(targetQueue.count( ! _.dead) / 4.0, multiplierSplash))
    val victimWasAlive    = victim.hitPoints > 0
    val damage            = realUnit.damageOnNextHitAgainst(victim.realUnit, Some(victim.shieldPoints), from = Some(pixel), to = Some(victim.pixel))
    val damageTotal       = Math.min(victim.hitPoints, damage)
    val damageToShields   = Math.min(victim.shieldPoints, damageTotal)
    val damageToHitPoints = damageTotal - damageToShields
    val valueDamage       = damageTotal * victim.valuePerDamage * With.configuration.simulationDamageValueRatio
    cooldownShooting      = (realUnit.cooldownMaxAgainst(victim.realUnit) / splashFactor).toInt
    cooldownMoving        += unitClass.stopFrames + unitClass.accelerationFrames / 2
    valueDealt            += valueDamage
    damageDealt           += damageTotal
    victim.valueReceived  += valueDamage
    victim.damageReceived += damageTotal
    victim.shieldPoints   -= damageToShields
    victim.hitPoints      -= damageToHitPoints
    dead                  = unitClass.suicides
    if (victimWasAlive && victim.hitPoints <= 0) {
      val valueKill = victim.realUnit.subjectiveValue * (1.0 - With.configuration.simulationDamageValueRatio)
      kills += 1
      valueDealt += valueKill
      victim.valueReceived += valueKill
    }

    val buildEvent = () => SimulationEventAttack(
      simulation.estimation.frames,
      this,
      victim,
      damageToHitPoints + damageToShields,
      victim.hitPoints <= 0
    )

    addEvent(buildEvent)
    victim.addEvent(buildEvent)
  }
  
  private def moveTowards(destination: Pixel) {
    if ( ! canMove)         return
    if (cooldownMoving > 0) return
    val effectiveSpeed    = topSpeed / pathBendiness
    val distancePerStep   = effectiveSpeed * SIMULATION_STEP_FRAMES
    val distanceBefore    = pixel.pixelDistance(destination)
    val distanceAfter     = Math.max(0.0, distanceBefore - effectiveSpeed * SIMULATION_STEP_FRAMES)
    val distanceTraveled  = distanceBefore - distanceAfter
    val framesTraveled    = (distanceTraveled / effectiveSpeed).toInt
    val pixelBefore       = pixel
    val pixelAfter        = pixel.project(destination, distanceTraveled)
    pixel                 = pixelAfter
    cooldownMoving        = framesTraveled
    cooldownShooting      = Math.max(cooldownMoving, cooldownShooting)
    addEvent(() => SimulationEventMove(
      simulation.estimation.frames,
      this,
      pixelBefore,
      pixelAfter,
      framesTraveled
    ))
  }
  
  private def addEvent(event: () => SimulationEvent) {
    if (ShowBattle.inUse) {
      events += event()
    }
  }
  
  def isValidTarget(target: Simulacrum): Boolean = (
    ! target.dead
    && target.hitPoints > 0
    && (pixelRangeMin <= 0 || pixel.pixelDistance(target.pixel) >= pixelRangeMin)
  )
  
  def pixelsOutOfRange(target: Simulacrum): Double = {
    val actualDistance = PurpleMath.broodWarDistanceBox(
      realUnit.pixelStartAt(pixel),
      realUnit.pixelEndAt(pixel),
      target.realUnit.pixelStartAt(target.pixel),
      target.realUnit.pixelEndAt(target.pixel))
    val output = actualDistance - realUnit.pixelRangeAgainst(target.realUnit) - bonusRange
    output
  }
  
  def validTargetExists       : Boolean = target.exists(isValidTarget)
  def validTargetExistsInRange: Boolean = validTargetExists && pixelsOutOfRange(target.get) <= 0
  
  def reportCard: ReportCard = ReportCard(
    simulacrum      = this,
    estimation      = simulation.estimation,
    valueDealt      = valueDealt,
    valueReceived   = valueReceived,
    damageDealt     = damageDealt,
    damageReceived  = damageReceived,
    dead            = dead,
    kills           = kills,
    events          = events
  )
}
