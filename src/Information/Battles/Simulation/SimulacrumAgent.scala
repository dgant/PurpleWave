package Information.Battles.Simulation

import Information.Battles.Simulation.Construction.{BattleSimulation, BattleSimulationGroup, Simulacrum}
import Information.Battles.Simulation.Tactics.{TacticFocus, TacticMovement, TacticWounded}
import Mathematics.Pixels.Pixel

class SimulacrumAgent(
  thisUnit  : Simulacrum,
  thisGroup : BattleSimulationGroup,
  thatGroup : BattleSimulationGroup,
  battle    : BattleSimulation) {
  
  // SimulacrumAgent.act() is probably the most-frequently called function in the codebase.
  // So it needs to be performant. Like, really, really performant.
  // It's really easy for a small performance regression in SimulacrumAgent to drop frames.
  //
  // Likely performance traps:
  // * Allocating memory (.filter(), .map())
  // * Slow UnitInfo calls (range checks, canAttack checks, etc.)
  // * Expensive math (Square roots)
  
  val chargingSpeedRatio = 0.75
  //What fraction of top speed charging units are likely to get
  val movementFrames = 8
  val wrongFocusPenalty = 10
  
  //////////////////
  // Update state //
  //////////////////
  
  def act() {
    if (!thisUnit.readyToAttack && !thisUnit.readyToMove) return
    updateThreat()
    updateTarget()
    updateFleeing()
    considerFleeing()
    considerAttacking()
    considerCharging()
    considerKiting()
  }
  
  private def updateFleeing() {
    if (thisUnit.canMove && ! thisUnit.fleeing) {
      thisUnit.fleeing ||= (thisGroup.tactics.movement == TacticMovement.Flee)
      thisUnit.fleeing ||= (thisGroup.tactics.wounded == TacticWounded.Flee && thisUnit.totalLife <= thisUnit.woundedThreshold)
      thisUnit.fleeing &&= thisUnit.threat.nonEmpty
    }
    if (thisUnit.fleeing) thisUnit.fighting = false
  }
  
  private def considerAttacking() = if (thisUnit.readyToAttack  && thisUnit.fighting) doAttack()
  private def considerFleeing()   = if (thisUnit.readyToMove    && thisUnit.fleeing)  doFlee()
  private def considerCharging()  = if (thisUnit.readyToMove    && thisUnit.fighting && thisGroup.tactics.movement == TacticMovement.Charge) doCharge()
  private def considerKiting()    = if (thisUnit.readyToMove    && thisGroup.tactics.movement == TacticMovement.Kite) {
    //TODO: If target is out of range
    if (thisUnit.fighting && thisUnit.target.exists(target => ! thisUnit.inRangeToAttack(target))) {
      doCharge()
    }
    else if (thisUnit.threat.nonEmpty) {
      doFlee()
    }
  }
  
  ////////////////////
  // Execute orders //
  ////////////////////
  
  private def doAttack()  = if (thisUnit.readyToAttack) thisUnit.target.foreach(target => if (thisUnit.inRangeToAttack(target)) dealDamage(target))
  private def doCharge()  = if (thisUnit.readyToMove)   thisUnit.target.foreach(target => moveTowards(target.pixel))
  private def doFlee()    = if (thisUnit.readyToMove)   thisUnit.threat.foreach(threat => moveAwayFrom(threat.pixel))
  
  private def dealDamage(target: Simulacrum) {
    val damage = thisUnit.unit.damageAgainst(target.unit, target.shields)
    thisUnit.attackCooldown = thisUnit.unit.cooldownAgainst(target.unit)
    thisUnit.moveCooldown   = Math.min(thisUnit.attackCooldown, 8)
    target.damageTaken  += damage
    target.shields      -= damage
    if (target.shields < 0) {
      target.hitPoints += target.shields
      target.shields = 0
    }
  }
  
  private def moveAwayFrom(destination: Pixel)  = move(destination, -1.0)
  private def moveTowards(destination: Pixel)   = move(destination, chargingSpeedRatio, thisUnit.pixel.pixelDistanceFast(destination))
  
  private def move(destination: Pixel, multiplier: Double, maxDistance: Double = 1000.0) {
    thisUnit.pixel = thisUnit.pixel.project(destination, Math.min(
      maxDistance,
      multiplier * thisUnit.topSpeed * (1 + movementFrames)))
    thisUnit.attackCooldown = movementFrames
    thisUnit.moveCooldown   = movementFrames
  }
  
  /////////////////////
  // Targets/threats //
  /////////////////////
  
  private def updateThreat() {
    if (thisUnit.threat.exists( ! _.alive)) {
      thisUnit.threat = None
    }
    if (thatGroup.units.exists(validThreat)) {
      thisUnit.threat = Some(thatGroup.units.minBy(threat =>
        if (validThreat(threat)) {
          thisUnit.pixel.pixelDistanceSquared(threat.pixel)
        } else 99999))
    }
  }
  
  private def updateTarget() {
    if (!thisUnit.fighting) {
      thisUnit.target = None
      return
    }
    if (thisUnit.target.exists(!_.alive)) {
      thisUnit.target = None
    }
    if (thisUnit.target.isEmpty) {
      if (thatGroup.units.exists(validTarget)) {
        thisUnit.target = Some(thatGroup.units.minBy(target =>
          if (validTarget(target)) {
            thisUnit.pixel.pixelDistanceSquared(target.pixel) *
              (if (   target.flying && thisGroup.tactics.focusAirOrGround == TacticFocus.Air)     1 else wrongFocusPenalty) *
              (if ( ! target.flying && thisGroup.tactics.focusAirOrGround == TacticFocus.Ground)  1 else wrongFocusPenalty) /
              target.totalLife
          } else Int.MaxValue))
      }
    }
  }
  
  def validTarget(target: Simulacrum) = target.alive && thisUnit.canAttack(target)
  def validThreat(threat: Simulacrum) = threat.alive && threat.canAttack(thisUnit)
}
