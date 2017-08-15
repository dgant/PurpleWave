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
  private val SIMULATION_STEP_FRAMES = 12
  
  lazy val targetQueue: mutable.PriorityQueue[Simulacrum] = (
    new mutable.PriorityQueue[Simulacrum]()(Ordering.by(x => (x.unit.unitClass.helpsInCombat, - x.pixel.pixelDistanceFast(pixel))))
      ++ unit.matchups.targets.flatMap(simulation.simulacra.get))
  
  val participating     : Boolean                     = (simulation.weAttack || unit.isEnemy) && unit.unitClass.helpsInCombat && ( ! unit.unitClass.isWorker || unit.isBeingViolent || unit.friendly.exists(_.agent.canFight))
  val canMove           : Boolean                     = unit.canMove
  var hitPoints         : Int                         = unit.totalHealth
  var cooldown          : Int                         = unit.cooldownLeft
  var pixel             : Pixel                       = unit.pixelCenter
  var dead              : Boolean                     = false
  var target            : Option[Simulacrum]          = None
  var atTarget          : Boolean                     = false
  var killed            : Int                         = 0
  var damageDealt       : Double                      = 0.0
  var damageReceived    : Double                      = 0.0
  var valueDealt        : Double                      = 0.0
  var valueReceived     : Double                      = 0.0
  var valuePerDamage    : Double                      = MicroValue.valuePerDamage(unit)
  var moves             : ArrayBuffer[(Pixel, Pixel)] = new ArrayBuffer[(Pixel, Pixel)]
  
  val maxSplashFactor = MicroValue.maxSplashFactor(unit)
  def splashFactor : Double = Math.max(1.0, Math.min(targetQueue.size / 4.0, maxSplashFactor))
  
  def checkDeath() {
    dead = dead || hitPoints <= 0
  }
  
  def step() {
    if (dead) {}
    else if ( ! participating) {
      val frames          = 8
      val focus           = simulation.focus
      val distanceBefore  = pixel.pixelDistanceFast(focus)
      val distanceAfter   = distanceBefore + unit.topSpeed * frames
      val fleePixel       = focus.project(pixel, distanceAfter)
      if (ShowBattleDetails.inUse) {
        moves += ((pixel, fleePixel))
      }
      pixel     = fleePixel
      cooldown  = frames
    }
    else if (cooldown > 0) {
      simulation.updated = true
      cooldown -= 1
    }
    else {
      acquireTarget()
      if (target.exists(valid)) {
        simulation.updated = true
        if (atTarget) {
          strikeTarget()
        }
        else {
          chaseTarget()
        }
      }
    }
  }
  
  def valid(target: Simulacrum): Boolean = {
    ! target.dead &&
    // Siege tanks
    (unit.pixelRangeMin <= 0 || pixel.pixelDistanceFast(target.pixel) >= unit.pixelRangeMin)
  }
  
  def acquireTarget() {
    while ( ! target.exists(valid) && targetQueue.nonEmpty) {
      atTarget = false
      if (canMove || targetQueue.headOption.exists(pixelsOutOfRange(_) <= 0.0)) {
        target = Some(targetQueue.dequeue())
        
        if ( ! canMove) {
          atTarget = true
        }
      } else {
        // Static defense
        cooldown = SIMULATION_STEP_FRAMES // Warning: This resets the timer on combat!
        return
      }
    }
  }
  
  def pixelsOutOfRange(simulacrum: Simulacrum): Double = {
    pixel.pixelDistanceFast(simulacrum.pixel) - unit.pixelRangeAgainstFromCenter(simulacrum.unit)
  }
  
  def chaseTarget() {
    val victim = target.get
    val pixelsFromRange = pixelsOutOfRange(victim)
    if (pixelsFromRange <= 0) {
      atTarget = true
    }
    else if (unit.canMove) {
      val travelFrames  = Math.min(SIMULATION_STEP_FRAMES, unit.framesToTravelPixels(pixelsFromRange))
      val travelPixel   = pixel.project(victim.pixel, unit.topSpeed * travelFrames)
      cooldown          = travelFrames
      pixel             = travelPixel
  
      if (ShowBattleDetails.inUse) {
        moves += ((pixel, travelPixel))
      }
    }
  }
  
  def strikeTarget() {
    val victim            = target.get
    val damage            = Math.min(target.get.hitPoints, unit.damageOnNextHitAgainst(victim.unit))
    val value             = damage * victim.valuePerDamage
    cooldown              = Math.max(1, (unit.cooldownMaxAgainst(victim.unit) / splashFactor).toInt)
    damageDealt           += damage
    valueDealt            += value
    victim.hitPoints      -= damage
    victim.damageReceived += damage
    victim.valueReceived  += value
    dead                  = dead || unit.unitClass.suicides
    
    if (unit.canStim && ! unit.stimmed) {
      cooldown = cooldown / 2
    }
  }
  
  def reportCard: ReportCard = ReportCard(
    estimation      = simulation.estimation,
    valueDealt      = valueDealt,
    valueReceived   = valueReceived,
    damageDealt     = damageDealt,
    damageReceived  = damageReceived,
    dead            = dead,
    killed          = killed
  )
}
