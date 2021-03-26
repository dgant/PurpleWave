package Information.Battles.Prediction.Simulation

import Debugging.Visualizations.Views.Battles.ShowBattles
import Lifecycle.With
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{CombatUnit, UnitInfo}
import ProxyBwapi.UnitTracking.Visibility

import scala.collection.mutable.ArrayBuffer

final class NewSimulacrum(baseUnit: UnitInfo) extends CombatUnit {
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
  var subjectiveValue: Double = _
  // Simulacrum properties
  var simulation: NewSimulation = _
  var behavior: SimulacrumBehavior = _
  val targets: ArrayBuffer[NewSimulacrum] = new ArrayBuffer[NewSimulacrum](50)
  var tile: Tile = _
  var cooldownMoving: Int = _
  var valuePerDamage: Double = _
  var kills: Int = _
  var damageDealt: Double = _
  var valueDealt: Double = _
  var damageReceived: Double = _
  var valueReceived: Double = _
  var events: Option[ArrayBuffer[SimulationEvent]] = _
  def reset(newSimulation: NewSimulation): Unit = {
    visibility = baseUnit.visibility
    player = baseUnit.player
    unitClass = baseUnit.unitClass
    pixel = baseUnit.pixel
    visible = baseUnit.visible
    alive = baseUnit.alive
    complete = baseUnit.complete
    burrowed = baseUnit.burrowed
    cloaked = baseUnit.cloaked
    detected = baseUnit.detected
    flying = baseUnit.flying
    plagued = baseUnit.plagued
    ensnared = baseUnit.ensnared
    invincible = baseUnit.invincible
    irradiated = baseUnit.irradiated
    lockedDown = baseUnit.lockedDown
    maelstrommed = baseUnit.maelstrommed
    stasised = baseUnit.stasised
    stimmed = baseUnit.stimmed
    hitPoints = baseUnit.hitPoints
    shieldPoints = baseUnit.shieldPoints
    matrixPoints = baseUnit.matrixPoints
    armorHealth = baseUnit.armorHealth
    armorShield = baseUnit.armorShield
    cooldownLeft = baseUnit.cooldownLeft
    loadedUnitCount = baseUnit.loadedUnitCount
    subjectiveValue = baseUnit.subjectiveValue
    // Simulacrum properties
    simulation = newSimulation
    behavior = BehaviorInitial
    targets.clear()
    tile = baseUnit.pixel.tile
    cooldownMoving = baseUnit.remainingFramesUntilMoving
    kills = 0
    damageDealt = 0
    valueDealt = 0
    damageReceived = 0
    valueReceived = 0
    events = if (ShowBattles.inUse) Some(new ArrayBuffer[SimulationEvent]()) else None
  }

  @inline def act(): Unit = {
    if (alive && (cooldownLeft == 0 || cooldownMoving == 0)) { behavior.act(this) }
  }

  @inline def update(): Unit = {
    if (alive) {
      if (hitPoints <= 0) {
        alive = false
        // TODO: If logging, add event
      } else {
        cooldownLeft = Math.max(0, cooldownLeft - 1)
        cooldownMoving = Math.max(0, cooldownMoving - 1)
        // TODO: Follow tweens
      }
    }
  }

  @inline def doBehavior(newBehavior: SimulacrumBehavior): Unit = {
    behavior = newBehavior
    // TODO: If logging, add event
    act()
  }

  @inline def tween(to: Pixel, frames: Int): Unit = {
    // TODO: Implement tween
    cooldownLeft = Math.max(cooldownLeft, frames)
    cooldownMoving = frames
    // TODO: If logging, add event
  }

  @inline private def dealDamage(victim: NewSimulacrum): Unit = {
    val victimWasAlive    = victim.hitPoints > 0
    val damage            = damageOnNextHitAgainst(victim, Some(victim.shieldPoints), from = Some(pixel), to = Some(victim.pixel))
    val damageTotal       = Math.min(victim.shieldPoints + victim.hitPoints, damage)
    val damageToShields   = Math.min(victim.shieldPoints, damageTotal)
    val damageToHitPoints = damageTotal - damageToShields
    val valueDamage       = damageTotal * victim.valuePerDamage * With.configuration.simulationDamageValueRatio
    cooldownLeft          = cooldownMaxAgainst(victim)
    cooldownMoving        += unitClass.stopFrames
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
    // TODO: If logging, add event
    /*
    val buildEvent = () => SimulationEventAttack(
      simulation.prediction.frames,
      this,
      victim,
      damageToHitPoints + damageToShields,
      victim.hitPoints <= 0
    )

    addEvent(buildEvent)
    victim.addEvent(buildEvent)
    */
  }

  @inline private def addEvent(event: () => SimulationEvent) { events.foreach(_ += event()) }
}
