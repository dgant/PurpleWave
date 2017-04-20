package Information.Battles.Simulation.Execution

import Information.Battles.Simulation.Construction.{BattleSimulation, BattleSimulationGroup, Simulacrum}
import Information.Battles.TacticsTypes.Tactics
import Mathematics.Pixels.Pixel

object SimulacrumAgent {
  
  // SimulacrumAgent.act() is probably the most-frequently called function in the codebase.
  // So it needs to be performant. Like, really, really performant.
  // It's really easy for a small performance regression in SimulacrumAgent to drop frames.
  //
  // Likely performance traps:
  // * Allocating memory (.filter(), .map())
  // * Slow UnitInfo calls (range checks, canAttack checks, etc.)
  // * Expensive math (Square roots)
  
  private val chargingSpeedRatio = 0.75
  //What fraction of top speed charging units are likely to get
  private val movementFrames = 8
  private val wrongFocusPenalty = 10
  
  //////////////////
  // Update state //
  //////////////////
  
  def act(
    thisUnit  : Simulacrum,
    thisGroup : BattleSimulationGroup,
    thatGroup : BattleSimulationGroup,
    battle    : BattleSimulation) {
    
    if ( ! thisUnit.alive) return
    thisUnit.attackCooldown = Math.max(0, thisUnit.attackCooldown - 1)
    thisUnit.moveCooldown   = Math.max(0, thisUnit.moveCooldown   - 1)
    if (! thisUnit.readyToAttack && ! thisUnit.readyToMove) return
    
    ///////////////////
    // Update threat //
    ///////////////////
  
    if (thisUnit.threat.exists( ! _.alive)) {
      thisUnit.threat = None
    }
    if (thisUnit.threat.isEmpty) {
      //Goal: We want to find the nearest threat.
      //The obvious way to do that is using minBy(). But minBy, like most Scala generics, causes boxing of primitives.
      //Primitive boxing is a huge, huge expense that we need to avoid in battle simulation.
      //So let's do this the old-school way!
      //And yes, Scala while-loops are way faster than for-loops.
      var bestScore = Int.MaxValue
      var i = 0
      while (i < thatGroup.units.size) {
        val threat = thatGroup.units(i)
        if (validThreat(thisUnit, threat)) {
          val score = thisUnit.pixel.pixelDistanceSquared(threat.pixel)
          if (score < bestScore) {
            bestScore = score
            thisUnit.threat = Some(threat)
          }
        }
        i += 1
      }
    }
  
    ///////////////////
    // Update target //
    ///////////////////
  
    if ( ! thisUnit.fighting) {
      thisUnit.target = None
      return
    }
    if (thisUnit.target.exists( ! _.alive)) {
      thisUnit.target = None
    }
    if (thisUnit.target.isEmpty) {
      //Goal: We want to find the best target.
      //The obvious way to do that is using minBy(). But minBy, like most Scala generics, causes boxing of primitives.
      //Primitive boxing is a huge, huge expense that we need to avoid in battle simulation.
      //So let's do this the old-school way!
      //And yes, Scala while-loops are way faster than for-loops.
      var bestScore = Int.MaxValue
      var i = 0
      while (i < thatGroup.units.size) {
        val target = thatGroup.units(i)
        if (validTarget(thisUnit, target)) {
          val score = thisUnit.pixel.pixelDistanceSquared(target.pixel) *
            (if (   target.flying && thisGroup.tactics.has(Tactics.Focus.Air))     1 else wrongFocusPenalty) *
            (if ( ! target.flying && thisGroup.tactics.has(Tactics.Focus.Ground))  1 else wrongFocusPenalty) /
            target.totalLife
          if (score < bestScore) {
            bestScore = score
            thisUnit.target = Some(target)
          }
        }
        i += 1
      }
    }
  
    //////////////////////////////
    // Update fight/flee status //
    //////////////////////////////
  
    if (thisUnit.canMove && ! thisUnit.fleeing) {
      thisUnit.fleeing ||= (thisGroup.tactics.has(Tactics.Movement.Flee))
      thisUnit.fleeing ||= (thisGroup.tactics.has(Tactics.Wounded.Flee) && thisUnit.totalLife <= thisUnit.woundedThreshold)
      thisUnit.fleeing &&= thisUnit.threat.nonEmpty
    }
    thisUnit.fighting &&= ! thisUnit.fleeing
  
    ////////////////////////
    // Consider attacking //
    ////////////////////////
  
    if (thisUnit.readyToAttack && thisUnit.fighting) {
      doAttack(thisUnit)
      return
    }
  
    /////////////////////////////////////////////////////////////////////////////
    // Shortcut: If the unit doesn't want to attack, and can't move, then quit //
    //                                                                         //
    // Everything afterwards assumes that the unit is ready to move.           //
    /////////////////////////////////////////////////////////////////////////////
    
    if ( ! thisUnit.readyToMove) {
      return
    }
  
    //////////////////////
    // Consider fleeing //
    //////////////////////
  
    if (thisUnit.fleeing) {
      doFlee(thisUnit)
      return
    }
  
    ///////////////////////
    // Consider charging //
    ///////////////////////

    else if (thisUnit.fighting && thisGroup.tactics.has(Tactics.Movement.Charge)) {
      doCharge(thisUnit)
      return
    }
  
    /////////////////////
    // Consider kiting //
    /////////////////////

    else if (thisGroup.tactics.has(Tactics.Movement.Kite)) {
      if (thisUnit.fighting && thisUnit.target.exists(target => ! thisUnit.inRangeToAttack(target))) {
        doCharge(thisUnit)
        return
      }
      else if (thisUnit.threat.nonEmpty) {
        doFlee(thisUnit)
        return
      }
    }
  }
  
  ////////////////////
  // Execute orders //
  ////////////////////
  
  @inline private def doAttack(thisUnit:Simulacrum)  = if (thisUnit.readyToAttack) thisUnit.target.foreach(target => if (thisUnit.inRangeToAttack(target)) dealDamage(thisUnit, target))
  @inline private def doCharge(thisUnit:Simulacrum)  = if (thisUnit.readyToMove)   thisUnit.target.foreach(target => moveTowards(thisUnit,  target.pixel))
  @inline private def doFlee(thisUnit:Simulacrum)    = if (thisUnit.readyToMove)   thisUnit.threat.foreach(threat => moveAwayFrom(thisUnit, threat.pixel))
  
  @inline private def dealDamage(thisUnit:Simulacrum, target: Simulacrum) {
    val damage = thisUnit.unit.damageAgainst(target.unit, target.shields)
    thisUnit.attackCooldown = thisUnit.unit.cooldownLeftAgainst(target.unit)
    thisUnit.moveCooldown   = Math.min(thisUnit.attackCooldown, 8)
    target.damageTaken      += damage
    target.shields          -= damage
    if (target.shields < 0) {
      target.hitPoints += target.shields
      target.shields = 0
    }
  }
  
  @inline private def moveAwayFrom(thisUnit:Simulacrum, destination: Pixel) {
    move(thisUnit, destination, -1.0)
  }
  @inline private def moveTowards(thisUnit:Simulacrum, destination: Pixel) {
    move(thisUnit, destination, chargingSpeedRatio, thisUnit.pixel.pixelDistanceFast(destination))
  }
  
  @inline private def move(thisUnit:Simulacrum, destination: Pixel, multiplier: Double, maxDistance: Double = 1000.0) {
    thisUnit.pixel = thisUnit.pixel.project(destination, Math.min(
      maxDistance,
      multiplier * thisUnit.topSpeed * (1 + movementFrames)))
    thisUnit.attackCooldown = movementFrames
    thisUnit.moveCooldown   = movementFrames
  }
  
  @inline def validTarget(thisUnit:Simulacrum, target: Simulacrum) = target.alive && thisUnit.canAttack(target)
  @inline def validThreat(thisUnit:Simulacrum, threat: Simulacrum) = threat.alive && threat.canAttack(thisUnit)
}
