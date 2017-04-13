package Information.Battles.Simulation

import Information.Battles.Simulation.Construction.{BattleSimulation, BattleSimulationGroup, Simulacrum}
import Information.Battles.Simulation.Tactics.{TacticFocus, TacticMovement, TacticWounded}
import Mathematics.Pixels.Pixel
import Utilities.EnrichPixel._

class SimulacrumAgent(
  thisUnit  : Simulacrum,
  thisGroup : BattleSimulationGroup,
  thatGroup : BattleSimulationGroup,
  battle    : BattleSimulation) {
  
  val chargingSpeedRatio = 0.75 //What fraction of top speed charging units are likely to get
  val movementFrames = 8
  
  def act() {
    if (!thisUnit.readyToAttack && !thisUnit.readyToMove) return
    updateFleeing()
    considerFleeing()
    considerAttacking()
    considerCharging()
    considerKiting()
  }
  
  private def updateFleeing() {
    if (thisUnit.canMove && ! thisUnit.fleeing) {
      thisUnit.fleeing ||= thisGroup.tactics.movement == TacticMovement.Flee
      thisUnit.fleeing ||=
        thisUnit.totalLife <= Math.min(20, thisUnit.unit.unitClass.maxTotalHealth / 3) &&
        (
          thisGroup.tactics.wounded == TacticWounded.Flee ||
          (
            thisGroup.tactics.wounded == TacticWounded.FleeRanged &&
            ! thisUnit.unit.melee
          )
        )
      thisUnit.fleeing &&= threats.nonEmpty
    }
    if (thisUnit.fleeing) thisUnit.fighting = false
  }
  
  private def considerAttacking() {
    if (thisUnit.readyToAttack && thisUnit.fighting) {
      doAttack()
    }
  }
  
  private def considerFleeing() {
    if (thisUnit.readyToMove && thisUnit.fleeing) {
      doFlee()
    }
  }
  
  private def considerCharging() {
    if (thisUnit.readyToMove && thisUnit.fighting && thisGroup.tactics.movement == TacticMovement.Charge) {
      doCharge()
    }
  }
  
  private def considerKiting() {
    if (thisUnit.readyToMove && thisGroup.tactics.movement == TacticMovement.Kite) {
      if (thisUnit.fighting && targetsInRange.isEmpty) {
        doCharge()
      }
      else if (threats.nonEmpty) {
        doFlee()
      }
    }
  }
  
  private def doAttack() {
    if (
      thisUnit.readyToAttack &&
      thisUnit.fighting &&
      targetsInRange.nonEmpty) {
      val target =
        if (thisGroup.tactics.focusAirOrGround == TacticFocus.Air) {
          val flyersInRange = targetsInRange.filter(_.unit.flying)
          if (flyersInRange.nonEmpty) lowestHealthTarget(flyersInRange) else lowestHealthTarget(targetsInRange)
        }
        else if (thisGroup.tactics.focusAirOrGround == TacticFocus.Ground) {
          val groundInRange = targetsInRange.filterNot(_.unit.flying)
          if (groundInRange.nonEmpty) lowestHealthTarget(groundInRange) else lowestHealthTarget(targetsInRange)
        }
        else {
          lowestHealthTarget(targetsInRange)
        }
    
      dealDamage(target)
    }
  }
  
  private def lowestHealthTarget(targets:Iterable[Simulacrum]):Simulacrum = {
    targets.minBy(_.totalLife)
  }
  
  private def doCharge() {
    if (targets.nonEmpty) {
      val target = targets.minBy(_.pixel.pixelDistanceSquared(thisUnit.pixel))
      moveTowards(target.pixel)
    }
  }
  
  private def doFlee() {
    if (threats.nonEmpty) {
      val closestThreat = threats.minBy(threat =>
        Math.min(
          threat.pixel.pixelDistanceFast(thisUnit.pixel),
          Math.max(0, threat.pixel.project(thisUnit.pixel, threat.unit.rangeAgainst(thisUnit.unit))
        .pixelDistanceSquared(thisUnit.pixel))))
      moveAwayFrom(closestThreat.pixel)
    }
  }
  
  private def dealDamage(target:Simulacrum) {
    val damage = thisUnit.unit.damageAgainst(target.unit, target.shields)
    thisUnit.attackCooldown = thisUnit.unit.cooldownAgainst(target.unit)
    thisUnit.moveCooldown = Math.min(thisUnit.attackCooldown, 8)
    target.damageTaken += damage
    target.shields -= damage
    if (target.shields < 0) {
      target.hitPoints += target.shields
      target.shields = 0
    }
  }
  
  private def moveAwayFrom(destination:Pixel) {
    move(destination, -1.0)
  }
  
  private def moveTowards(destination:Pixel) {
    move(destination, chargingSpeedRatio, thisUnit.pixel.pixelDistanceFast(destination))
  }
  
  private def move(destination:Pixel, multiplier:Double, maxDistance:Double = 1000.0) {
    thisUnit.pixel = thisUnit.pixel.project(destination, Math.min(
      maxDistance,
      multiplier * thisUnit.topSpeed * (1 + movementFrames)))
    thisUnit.attackCooldown = movementFrames
    thisUnit.moveCooldown = movementFrames
  }

  private lazy val threats = thatGroup.units.filter(_.canAttack(thisUnit))
  private lazy val targets = thatGroup.units.filter(thisUnit.canAttack(_))
  private lazy val targetsInRange = for (target <- targets if thisUnit.inRangeToAttack(target)) yield target
  private lazy val threatsInRange = for (threat <- threats if Math.pow(threat.rangeAgainst(thisUnit), 2) >= threat.pixel.pixelDistanceSquared(thisUnit.pixel)) yield threat
}
