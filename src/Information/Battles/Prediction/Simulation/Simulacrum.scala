package Information.Battles.Prediction.Simulation

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile}
import ProxyBwapi.Size
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{CombatUnit, DamageDealt, UnitInfo}
import ProxyBwapi.UnitTracking.{UnorderedBuffer, Visibility}
import Utilities.{?, LightYear}

import scala.collection.mutable.ArrayBuffer

final class Simulacrum(val realUnit: UnitInfo) extends CombatUnit {
  var initialized     : Boolean = false
  // Real unit properties
  var visibility              : Visibility.Value = _
  var player                  : PlayerInfo = _
  var unitClass               : UnitClass = _
  var unitSize                : Size.Type = _
  var unitSizeCode            : Int = _
  var unitDimensionMin        : Int = _
  var affectedByDarkSwarm     : Boolean = _
  var damageAirLarge          : Double = _
  var damageAirMedium         : Double = _
  var damageAirSmall          : Double = _
  var damageGroundLarge       : Double = _
  var damageGroundMedium      : Double = _
  var damageGroundSmall       : Double = _
  var pixel                   : Pixel = _
  var pixelLeft               : Int = _
  var pixelTop                : Int = _
  var pixelRightExclusive     : Int = _
  var pixelBottomExclusive    : Int = _
  var altitude                : Int = _
  var visible                 : Boolean = _
  var alive                   : Boolean = _
  var complete                : Boolean = _
  var burrowed                : Boolean = _
  var cloaked                 : Boolean = _
  var detected                : Boolean = _
  var flying                  : Boolean = _
  var plagued                 : Boolean = _
  var ensnared                : Boolean = _
  var invincible              : Boolean = _
  var irradiated              : Boolean = _
  var lockedDown              : Boolean = _
  var maelstrommed            : Boolean = _
  var stasised                : Boolean = _
  var stimmed                 : Boolean = _
  var hitPoints               : Int = _
  var shieldPoints            : Int = _
  var matrixPoints            : Int = _
  var armorHealth             : Int = _
  var armorShield             : Int = _
  var cooldownLeft            : Int = _
  var loadedUnitCount         : Int = _
  var angleRadians            : Double = _
  var speed                   : Double = _
  var topSpeed                : Double = _
  var topSpeedPossible        : Double = _
  var subjectiveValue         : Double = _
  var _targetValue            : Double = _
  var injury                  : Double = _
  var isFriendlyCached        : Boolean = _
  var attacksAgainstAirCached    : Int    = _
  var attacksAgainstGroundCached : Int    = _
  var pixelRangeAirCached        : Double = _
  var pixelRangeGroundCached     : Double = _
  // Simulacrum properties
  var simulation              : Simulation = _
  var behavior                : SimulacrumBehavior = BehaviorInitial
  var target                  : Option[Simulacrum] = None
  var threat                  : Option[Simulacrum] = None
  val targets                 : UnorderedBuffer[Simulacrum] = new UnorderedBuffer[Simulacrum](50)
  var cooldownTargeting       : Int = _
  var gridTile                : Option[SimulationGridTile] = None
  var measureHealth           : Boolean = _
  var hitPointsInitial        : Int = _
  var shieldPointsInitial     : Int = _
  var cooldownMoving          : Int = _
  var moveGoal                : Option[Pixel] = None
  var moveGoalDistanceSquared : Double  = _
  var lastTile                : Option[Tile] = None
  var valuePerDamage          : Double  = _
  var shouldScoreIfOurs       : Boolean = _
  var kills                   : Int     = _
  var damageDealt             : Double  = _
  var valueDealt              : Double  = _
  var damageReceived          : Double  = _
  var valueReceived           : Double  = _
  private var recordedEvents  : ArrayBuffer[SimulationEvent] = _
  def events: Seq[SimulationEvent] = if (recordedEvents == null) Vector.empty else recordedEvents
  private[Simulation] def clearEvents(): Unit = if (recordedEvents != null) recordedEvents.clear()

  def reset(newSimulation: Simulation): Unit = {
    initialized             = true
    simulation              = newSimulation
    visibility              = realUnit.visibility
    player                  = realUnit.player
    unitClass               = realUnit.unitClass
    unitSize                = unitClass.size
    unitSizeCode            = if (unitSize == Size.Large) 2 else if (unitSize == Size.Medium) 1 else 0
    unitDimensionMin        = unitClass.dimensionMin
    affectedByDarkSwarm     = unitClass.affectedByDarkSwarm
    damageAirLarge          = unitClass.airDamageType.damageAgainstLarge
    damageAirMedium         = unitClass.airDamageType.damageAgainstMedium
    damageAirSmall          = unitClass.airDamageType.damageAgainstSmall
    damageGroundLarge       = unitClass.groundDamageType.damageAgainstLarge
    damageGroundMedium      = unitClass.groundDamageType.damageAgainstMedium
    damageGroundSmall       = unitClass.groundDamageType.damageAgainstSmall
    setPixel(realUnit.pixel)
    visible                 = realUnit.visible
    alive                   = realUnit.alive
    complete                = realUnit.complete
    burrowed                = realUnit.burrowed
    cloaked                 = realUnit.cloaked
    detected                = realUnit.detected
    flying                  = realUnit.flying
    plagued                 = realUnit.plagued
    ensnared                = realUnit.ensnared
    invincible              = realUnit.invincible
    irradiated              = realUnit.irradiated
    lockedDown              = realUnit.lockedDown
    maelstrommed            = realUnit.maelstrommed
    stasised                = realUnit.stasised
    stimmed                 = realUnit.stimmed
    hitPoints               = realUnit.hitPoints
    shieldPoints            = realUnit.shieldPoints
    matrixPoints            = realUnit.matrixPoints
    armorHealth             = realUnit.armorHealth
    armorShield             = realUnit.armorShield
    cooldownLeft            = realUnit.cooldownLeft
    loadedUnitCount         = realUnit.loadedUnitCount
    angleRadians            = realUnit.angleRadians
    speed                   = 0 // TODO: Model speed and start with baseUnit.speed
    topSpeed                = realUnit.topSpeed
    topSpeedPossible        = realUnit.topSpeedPossible
    subjectiveValue         = realUnit.subjectiveValue
    _targetValue            = realUnit.targetValue
    recalculateInjury()
    isFriendlyCached        = realUnit.isFriendly
    snapshotWeapons()
    // Simulacrum properties
    simulation              = newSimulation
    behavior                = BehaviorInitial
    target                  = None
    threat                  = None
    targets.clear()
    cooldownTargeting       = 0
    measureHealth           = true
    hitPointsInitial        = realUnit.hitPoints
    shieldPointsInitial     = realUnit.shieldPoints
    cooldownMoving          = realUnit.remainingFramesUntilMoving
    moveGoal                = None
    moveGoalDistanceSquared = 0
    lastTile                = None
    shouldScoreIfOurs       = unitClass.attacksOrCastsOrDetectsOrTransports
    kills                   = 0
    damageDealt             = 0
    valueDealt              = 0
    damageReceived          = 0
    valueReceived           = 0
    clearEvents()
    simulation.grid.populate(this)
  }

  // Weapon capabilities and upgrades stay consistent with the rest of this simulation snapshot.
  private[Simulation] def snapshotWeapons(): Unit = {
    attacksAgainstAirCached    = realUnit.attacksAgainstAir
    attacksAgainstGroundCached = realUnit.attacksAgainstGround
    pixelRangeAirCached        = realUnit.pixelRangeAir
    pixelRangeGroundCached     = realUnit.pixelRangeGround
  }

  @inline override def attacksAgainstAir    : Int    = attacksAgainstAirCached
  @inline override def attacksAgainstGround : Int    = attacksAgainstGroundCached
  @inline override def pixelRangeAir        : Double = pixelRangeAirCached
  @inline override def pixelRangeGround     : Double = pixelRangeGroundCached
  @inline def occupancy: Int = ?(flying, 0, unitClass.occupancy)
  @inline def canMove: Boolean = topSpeedPossible > 0
  @inline def setPixel(newPixel: Pixel): Unit = {
    pixel = newPixel
    pixelLeft = newPixel.x - unitClass.dimensionLeft
    pixelTop = newPixel.y - unitClass.dimensionUp
    pixelRightExclusive = newPixel.x + unitClass.dimensionRightExclusive
    pixelBottomExclusive = newPixel.y + unitClass.dimensionDownExclusive
    altitude = With.game.getGroundHeight(Maff.div32(newPixel.x), Maff.div32(newPixel.y))
  }
  @inline def pixelDistanceEdgeSimulation(other: Simulacrum): Double = {
    Maff.broodWarDistanceBox(
      pixelLeft, pixelTop, pixelRightExclusive, pixelBottomExclusive,
      other.pixelLeft, other.pixelTop, other.pixelRightExclusive, other.pixelBottomExclusive)
  }
  @inline def inRangeToAttackSimulation(target: Simulacrum): Boolean = {
    val distance = pixelDistanceEdgeSimulation(target)
    distance <= pixelRangeAgainst(target) && (pixelRangeMin <= 0.0 || distance > pixelRangeMin)
  }
  @inline def pixelsToGetInRangeSimulation(target: Simulacrum): Double = {
    ?(canAttack(target), pixelDistanceEdgeSimulation(target) - pixelRangeAgainst(target), LightYear())
  }
  @inline def canAttack(other: CombatUnit): Boolean = ((other.flying && attacksAgainstAir > 0) || ( ! other.flying && attacksAgainstGround > 0)) && (other.detected || ! other.cloaked)
  @inline def targetValue: Double = _targetValue
  @inline def act(): Unit = { if (alive && (cooldownLeft == 0 || cooldownMoving == 0)) { behavior.act(this) } }

  @inline def update(): Unit = {
    threat = threat.filter(t => t.alive && t.target.contains(this))
    if ( ! alive && gridTile.isDefined) simulation.grid.remove(this)
    if (alive) {
      if (hitPoints <= 0) {
        alive = false
        simulation.grid.remove(this)
        if (simulation.battle.logSimulation) addEvent(SimulationEventDeath(this))
      } else {
        cooldownLeft      = Math.max(0, cooldownLeft      - 1)
        cooldownMoving    = Math.max(0, cooldownMoving    - 1)
        cooldownTargeting = Math.max(0, cooldownTargeting - 1)
        val yielded = (moveGoal.isDefined || cooldownMoving == 0) && simulation.grid.tryYield(this)
        if (moveGoal.isDefined && ! yielded) {
          val goal = moveGoal.get
          if (pixel.pixelDistanceSquared(goal) > moveGoalDistanceSquared) {
            simulation.grid.tryMove(this, goal)
          } else {
            moveGoal = None
          }
        }
      }
    }
  }

  @inline def doBehavior(newBehavior: SimulacrumBehavior): Unit = {
    if (simulation.battle.logSimulation) addEvent(SimulationEventBehavior(this, behavior, newBehavior))
    behavior = newBehavior
    act()
  }

  @inline def setTarget(targetNew: Option[Simulacrum]): Unit = {
    if (target != targetNew) {
      if (simulation.battle.logSimulation) addEvent(SimulationEventSwitchTarget(this, target, targetNew))
    }
    target = targetNew
  }

  @inline def move(goal: => Pixel, reason: => Option[String], goalDistance: Double = 0): Unit = {
    if (cooldownMoving > 0) return
    val ableToMove = canMove && topSpeed > 0
    val destination = if (ableToMove || simulation.battle.logSimulation) goal else null
    if (ableToMove) {
      var goalAdjusted        = destination.clamp(unitClass.dimensionMax)
      goalAdjusted            = ?(flying, goalAdjusted, goalAdjusted.walkablePixel)
      moveGoal                = Some(goalAdjusted)
      moveGoalDistanceSquared = goalDistance * goalDistance
      cooldownMoving          = Maff.clamp(0, With.configuration.simulationResolution, (0.999 + (pixelDistanceCenter(destination) - goalDistance) / topSpeed).toInt)
    }
    if (simulation.battle.logSimulation) addEvent(SimulationEventMove(this, destination, ?(ableToMove, reason, Some("(Stuck due to immobility)"))))
  }

  @inline def recalculateInjury(): Unit = {
    injury = _calculateInjury
  }

  @inline def sleep(frames: Int, reason: => Option[String] = None): Unit = {
    if (simulation.battle.logSimulation) addEvent(SimulationEventSleep(this, frames, reason))
    cooldownLeft = Math.max(cooldownLeft, frames)
    cooldownMoving = Math.max(cooldownMoving, frames)
  }

  @inline def dealDamageTo(victim: Simulacrum): Unit = {
    val volley = damageVolleyAgainst(victim, from = Some(pixel), to = Some(victim.pixel))
    applyAttackDamage(victim, volley(victim.shieldPoints))
    simulation.grid.applySplash(victim, volley, isFriendlyCached, realUnit, pixel, this)
    cooldownLeft = cooldownMaxAgainst(victim)
    cooldownMoving = Math.max(cooldownMoving, unitClass.stopFrames)
    alive = alive && ! unitClass.suicides
  }

  private[Simulation] def applyAttackDamage(victim: Simulacrum, damage: DamageDealt): Unit = {
    val victimWasAlive = victim.hitPoints > 0
    val damageTotal = victim.receiveDamage(damage)
    if (damageTotal > 0) simulation.interactions.observeDamage(realUnit, victim.realUnit)
    val credit = if (isFriendlyCached == victim.isFriendlyCached) -1 else 1
    valueDealt += credit * damageTotal * victim.valuePerDamage * With.configuration.simulationDamageValueRatio
    damageDealt += credit * damageTotal
    if (victimWasAlive && victim.hitPoints <= 0) {
      val valueKill = victim.subjectiveValue * (1.0 - With.configuration.simulationDamageValueRatio)
      if (credit > 0) kills += 1
      valueDealt += credit * valueKill
      victim.valueReceived += valueKill
    }
    if (simulation.battle.logSimulation) addEvent(SimulationEventAttack(
      this,
      victim,
      damageTotal,
      victim.hitPoints <= 0))
  }

  @inline def receiveDamage(damage: DamageDealt): Int = {
    val toHealth = Math.min(hitPoints, Math.round(damage.health).toInt)
    val toShields = Math.min(shieldPoints, Math.round(damage.shields).toInt)
    hitPoints -= toHealth
    shieldPoints -= toShields
    val total = toHealth + toShields
    damageReceived += total
    valueReceived += total * valuePerDamage * With.configuration.simulationDamageValueRatio
    recalculateInjury()
    total
  }

  @inline private def addEvent(event: SimulationEvent): Unit = {
    if (recordedEvents == null) recordedEvents = ArrayBuffer.empty
    recordedEvents += event
  }

  def describe: String = f"${?(isFriendly, "F", "E")} ${unitClass.toString} #${realUnit.id}"

  override def toString: String = f"$describe $hitPoints/${unitClass.maxHitPoints} ${if (shieldPoints > 0) f"($shieldPoints/${unitClass.maxShields})" else  ""} $pixel"
}
