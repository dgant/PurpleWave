package Information.Battles.Prediction.Simulation

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.Maff
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{CombatUnit, UnitInfo}
import ProxyBwapi.UnitTracking.{UnorderedBuffer, Visibility}
import Utilities.Time.Forever

import scala.collection.mutable.ArrayBuffer

final class Simulacrum(val realUnit: UnitInfo) extends CombatUnit {
  var initialized: Boolean = false
  // Real unit properties
  var visibility: Visibility.Value = _
  var player: PlayerInfo = _
  var unitClass: UnitClass = _
  var pixel: Pixel = _
  var visible: Boolean = _
  var alive: Boolean = _
  var complete: Boolean = _
  var burrowed: Boolean = _
  var cloaked: Boolean = _
  var detected: Boolean = _
  var flying: Boolean = _
  var plagued: Boolean = _
  var ensnared: Boolean = _
  var invincible: Boolean = _
  var irradiated: Boolean = _
  var lockedDown: Boolean = _
  var maelstrommed: Boolean = _
  var stasised: Boolean = _
  var stimmed: Boolean = _
  var hitPoints: Int = _
  var shieldPoints: Int = _
  var matrixPoints: Int = _
  var armorHealth: Int = _
  var armorShield: Int = _
  var cooldownLeft: Int = _
  var loadedUnitCount: Int = _
  var angleRadians: Double = _
  var speed: Double = _
  var topSpeed: Double = _
  var topSpeedPossible: Double = _
  var subjectiveValue: Double = _
  // Simulacrum properties
  var simulation: Simulation = _
  var behavior: SimulacrumBehavior = _
  var target: Option[Simulacrum] = _
  var threat: Option[Simulacrum] = _
  val targets: UnorderedBuffer[Simulacrum] = new UnorderedBuffer[Simulacrum](50)
  var gridTile: SimulationGridTile = _
  var measureHealth: Boolean = _
  var hitPointsInitial: Int = _
  var shieldPointsInitial: Int = _
  var cooldownMoving: Int = _
  var tweenFramesDone: Int = _
  var tweenFramesLeft: Int = 0
  var tweenFrom: Pixel = _
  var tweenGoal: Pixel = _
  var valuePerDamage: Double = _
  var kills: Int = _
  var damageDealt: Double = _
  var valueDealt: Double = _
  var damageReceived: Double = _
  var valueReceived: Double = _
  var events: ArrayBuffer[SimulationEvent] = ArrayBuffer.empty
  def reset(newSimulation: Simulation): Unit = {
    initialized = true
    simulation = newSimulation
    visibility = realUnit.visibility
    player = realUnit.player
    unitClass = realUnit.unitClass
    pixel = realUnit.pixel
    gridTile = simulation.grid.tiles(pixel.tile.i)
    gridTile += this
    visible = realUnit.visible
    alive = realUnit.alive
    complete = realUnit.complete
    burrowed = realUnit.burrowed
    cloaked = realUnit.cloaked
    detected = realUnit.detected
    flying = realUnit.flying
    plagued = realUnit.plagued
    ensnared = realUnit.ensnared
    invincible = realUnit.invincible
    irradiated = realUnit.irradiated
    lockedDown = realUnit.lockedDown
    maelstrommed = realUnit.maelstrommed
    stasised = realUnit.stasised
    stimmed = realUnit.stimmed
    hitPoints = realUnit.hitPoints
    shieldPoints = realUnit.shieldPoints
    matrixPoints = realUnit.matrixPoints
    armorHealth = realUnit.armorHealth
    armorShield = realUnit.armorShield
    cooldownLeft = realUnit.cooldownLeft
    loadedUnitCount = realUnit.loadedUnitCount
    angleRadians = realUnit.angleRadians
    speed = 0 // TODO: Model speed and start with baseUnit.speed
    topSpeed = realUnit.topSpeed
    topSpeedPossible = realUnit.topSpeedPossible
    subjectiveValue = realUnit.subjectiveValue
    // Simulacrum properties
    simulation = newSimulation
    behavior = BehaviorInitial
    target = None
    threat = None
    targets.clear()
    measureHealth = true
    hitPointsInitial = realUnit.hitPoints
    shieldPointsInitial = realUnit.shieldPoints
    cooldownMoving = realUnit.remainingFramesUntilMoving
    tweenFramesLeft = 0
    kills = 0
    damageDealt = 0
    valueDealt = 0
    damageReceived = 0
    valueReceived = 0
    events.clear()
  }

  @inline def canMove: Boolean = topSpeedPossible > 0
  @inline def canAttack(other: CombatUnit): Boolean = (other.flying && attacksAgainstAir > 0) || ( ! other.flying && attacksAgainstGround > 0)
  @inline def act(): Unit = { if (alive && (cooldownLeft == 0 || cooldownMoving == 0)) { behavior.act(this) } }

  @inline def update(): Unit = {
    threat = threat.filter(t => t.alive && t.target.contains(this))
    if (alive) {
      if (hitPoints <= 0) {
        alive = false
        // TODO: If logging, add event
      } else {
        cooldownLeft = Math.max(0, cooldownLeft - 1)
        cooldownMoving = Math.max(0, cooldownMoving - 1)
        if (tweenFramesLeft > 0) {
          tweenFramesDone += 1
          tweenFramesLeft -= 1
          // TODO: Account for adjusted paths from eg. moving around obstacles
          simulation.grid.tryMove(this, if (tweenFramesLeft == 0) tweenGoal else tweenFrom.project(tweenGoal, topSpeed * tweenFramesDone))
        }
      }
    }
  }

  @inline def doBehavior(newBehavior: SimulacrumBehavior): Unit = {
    if (simulation.battle.logSimulation) {
      addEvent(SimulationEventBehavior(this, behavior, newBehavior))
    }
    behavior = newBehavior
    act()
  }

  @inline def setTarget(targetNew: Option[Simulacrum]): Unit = {
    if (simulation.battle.logSimulation && target != targetNew) {
      addEvent(SimulationEventSwitchTarget(this, target, targetNew))
    }
    target = targetNew
  }

  @inline def tween(to: Pixel, frames: Int, reason: Option[String] = None): Unit = {
    if (simulation.battle.logSimulation) {
      addEvent(SimulationEventTween(this, to, frames, reason))
    }
    cooldownMoving = Maff.clamp(frames, 1, With.configuration.simulationResolution)
    cooldownLeft = Math.max(cooldownLeft, cooldownMoving)
    tweenFrom = pixel
    tweenGoal = to
    tweenFramesDone = 0
    tweenFramesLeft = cooldownMoving
  }
  @inline def tween(to: Pixel, reason: Option[String]): Unit = {
    tween(to, (0.999 + Maff.nanToN(pixelDistanceCenter(to) / topSpeedPossible, Forever())).toInt, reason)
  }
  @inline def tween(to: Pixel): Unit = {
    tween(to, None)
  }

  @inline def sleep(frames: Int, reason: Option[String] = None): Unit = {
    if (simulation.battle.logSimulation) {
      addEvent(SimulationEventSleep(this, frames, reason))
    }
    cooldownLeft = Math.max(cooldownLeft, frames)
    cooldownMoving = Math.max(cooldownMoving, frames)
  }

  @inline def dealDamageTo(victim: Simulacrum): Unit = {
    val victimWasAlive    = victim.hitPoints > 0
    val damage            = damageOnNextHitAgainst(victim, Some(victim.shieldPoints), from = Some(pixel), to = Some(victim.pixel))
    val damageTotal       = Math.min(victim.shieldPoints + victim.hitPoints, damage)
    val damageToShields   = Math.min(victim.shieldPoints, damageTotal)
    val damageToHitPoints = damageTotal - damageToShields
    val valueDamage       = damageTotal * victim.valuePerDamage * With.configuration.simulationDamageValueRatio
    cooldownLeft          = cooldownMaxAgainst(victim)
    cooldownMoving        = Math.max(cooldownMoving, unitClass.stopFrames)
    valueDealt            += valueDamage
    damageDealt           += damageTotal
    victim.valueReceived  += valueDamage
    victim.damageReceived += damageTotal
    victim.shieldPoints   -= damageToShields
    victim.hitPoints      -= damageToHitPoints
    alive                 = alive && ! unitClass.suicides
    if (victimWasAlive && victim.hitPoints <= 0) {
      val valueKill = victim.subjectiveValue * (1.0 - With.configuration.simulationDamageValueRatio)
      kills += 1
      valueDealt += valueKill
      victim.valueReceived += valueKill
    }
    if (simulation.battle.logSimulation) {
      addEvent(SimulationEventAttack(
        this,
        victim,
        damageToHitPoints + damageToShields,
        victim.hitPoints <= 0))
    }
  }

  @inline private def addEvent(event: SimulationEvent) { events += event }
}
