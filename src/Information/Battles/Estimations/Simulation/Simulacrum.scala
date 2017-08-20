package Information.Battles.Estimations.Simulation

import Debugging.Visualizations.Views.Battles.ShowBattleDetails
import Information.Battles.Estimations.ReportCard
import Mathematics.Points.Pixel
import Micro.Decisions.MicroValue
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case class Simulacrum(simulation: BattleSimulation, unit: UnitInfo) {
  
  // Constant
  private val SIMULATION_STEP_FRAMES = 8
  
  val canMove           : Boolean                     = unit.canMove
  val topSpeed          : Double                      = unit.topSpeed
  var hitPoints         : Int                         = unit.totalHealth
  var cooldownShooting  : Int                         = 1 + unit.cooldownLeft
  var cooldownMoving    : Int                         = 0
  var pixel             : Pixel                       = unit.pixelCenter
  var dead              : Boolean                     = false
  var target            : Option[Simulacrum]          = None
  val maxSplashFactor   : Double                      = MicroValue.maxSplashFactor(unit)
  var killed            : Int                         = 0
  var damageDealt       : Double                      = 0.0
  var damageReceived    : Double                      = 0.0
  var valueDealt        : Double                      = 0.0
  var valueReceived     : Double                      = 0.0
  var valuePerDamage    : Double                      = MicroValue.valuePerDamage(unit)
  var moves             : ArrayBuffer[(Pixel, Pixel)] = new ArrayBuffer[(Pixel, Pixel)]
  
  lazy val targetQueue: mutable.PriorityQueue[Simulacrum] = (
    new mutable.PriorityQueue[Simulacrum]()(Ordering.by(x => (x.unit.unitClass.helpsInCombat, - x.pixel.pixelDistanceFast(pixel))))
      ++ unit.matchups.targets
        .filter(target =>
          unit.inRangeToAttackFast(target)
          || simulation.weAttack
          || ! unit.isOurs)
        .flatMap(simulation.simulacra.get)
    )
  
  val fighting: Boolean = {
    if ( ! unit.unitClass.helpsInCombat) {
      false
    }
    else if (unit.unitClass.isWorker) {
      unit.isBeingViolent || ( ! unit.gathering && ! unit.constructing)
    }
    else if (unit.isEnemy) {
      true
    }
    else if (simulation.weAttack) {
      unit.canMove || unit.matchups.targetsInRange.nonEmpty
    }
    else {
      ! unit.canMove
    }
  }
  
  def splashFactor : Double = if (maxSplashFactor == 1.0) 1.0 else Math.max(1.0, Math.min(targetQueue.count( ! _.dead) / 4.0, maxSplashFactor))
  
  def updateDeath() {
    dead = dead || hitPoints <= 0
  }
  
  def step() {
    if (dead) return
    cooldownMoving    -= 1
    cooldownShooting  -= 1
    tryToAcquireTarget()
    tryToChaseTarget()
    tryToStrikeTarget()
    tryToRunAway()
    if (cooldownShooting > 0)           simulation.updated = true
    if (cooldownMoving > 0 && fighting) simulation.updated = true
  }
  
  def tryToAcquireTarget() {
    if ( ! fighting)                                return
    if (cooldownMoving > 0 || cooldownShooting > 0) return
    if (targetExistsInRange)                        return
    
    if (targetExists) {
      //Target is out of range, but maybe we can get them later. Put them back in the queue.
      targetQueue += target.get
    }
    
    target = None
    while (target.isEmpty && targetQueue.nonEmpty) {
      
      // Clear crud from the queue
      while (targetQueue.nonEmpty && ! valid(targetQueue.head)) {
        targetQueue.dequeue()
      }
      
      if (targetQueue.nonEmpty) {
        target = Some(targetQueue.dequeue())
      }
    }
  }
  
  def tryToChaseTarget() {
    lazy val victim          = target.get
    lazy val pixelsFromRange = pixelsOutOfRange(victim)
    if ( ! canMove)           return
    if ( ! fighting)          return
    if (cooldownMoving > 0)   return
    if ( ! targetExists)      return
    if (pixelsFromRange <= 0) return
    
    val travelFrames    = Math.min(SIMULATION_STEP_FRAMES, unit.framesToTravelPixels(pixelsFromRange))
    val travelPixel     = pixel.project(victim.pixel, unit.topSpeed * travelFrames)
    val originalPixel   = pixel
    cooldownMoving      = travelFrames
    pixel               = travelPixel
    simulation.updated  = true
  
    if (ShowBattleDetails.inUse) {
      moves += ((originalPixel, travelPixel))
    }
  }
  
  def tryToStrikeTarget() {
    if ( ! fighting)            return
    if (cooldownShooting > 0)   return
    if ( ! targetExistsInRange) return
    val victim            = target.get
    val damage            = Math.min(target.get.hitPoints, unit.damageOnNextHitAgainst(victim.unit))
    val value             = damage * victim.valuePerDamage
    cooldownShooting      = Math.max(1, (unit.cooldownMaxAgainst(victim.unit) / splashFactor).toInt)
    cooldownMoving        = Math.max(cooldownMoving, cooldownMoving + unit.unitClass.stopFrames)
    damageDealt           += damage
    valueDealt            += value
    victim.hitPoints      -= damage
    victim.damageReceived += damage
    victim.valueReceived  += value
    dead                  = dead || unit.unitClass.suicides
    simulation.updated    = true
  
    if (unit.canStim && ! unit.stimmed) {
      cooldownShooting = cooldownShooting / 2
    }
  }
  
  def tryToRunAway() {
    if (fighting)           return
    if ( ! canMove)         return
    if (cooldownMoving > 0) return
    val focus           = simulation.focus
    val distanceBefore  = pixel.pixelDistanceFast(focus)
    val distanceAfter   = distanceBefore + unit.topSpeed * SIMULATION_STEP_FRAMES
    val fleePixel       = focus.project(pixel, distanceAfter)
    if (ShowBattleDetails.inUse) {
      moves += ((pixel, fleePixel))
    }
    pixel           = fleePixel
    cooldownMoving  = SIMULATION_STEP_FRAMES
  }
  
  def valid(target: Simulacrum): Boolean = {
    ! target.dead && (unit.pixelRangeMin <= 0 || pixel.pixelDistanceFast(target.pixel) >= unit.pixelRangeMin)
  }
  
  def pixelsOutOfRange(simulacrum: Simulacrum): Double = {
    pixel.pixelDistanceFast(simulacrum.pixel) - unit.pixelRangeAgainstFromCenter(simulacrum.unit)
  }
  
  def targetExists: Boolean = target.exists(valid)
  
  def targetExistsInRange: Boolean = {
    target.exists(valid) && pixelsOutOfRange(target.get) <= 0
  }
  
  def reportCard: ReportCard = ReportCard(
    simulacrum      = this,
    estimation      = simulation.estimation,
    valueDealt      = valueDealt,
    valueReceived   = valueReceived,
    damageDealt     = damageDealt,
    damageReceived  = damageReceived,
    dead            = dead,
    killed          = killed
  )
}
