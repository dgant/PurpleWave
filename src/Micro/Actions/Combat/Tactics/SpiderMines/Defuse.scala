package Micro.Actions.Combat.Tactics.SpiderMines

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Commands.{Attack, Patrol}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable

class Defuse(defuser: UnitInfo, mine: UnitInfo) extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = true

  lazy val helpers: Iterable[UnitInfo] = {
    var damageRequired = mine.unitClass.maxTotalHealth
    damageRequired -= defuser.damageOnNextHitAgainst(mine)

    if (damageRequired == 0) {
      Iterable.empty
    }
    else {
      var output: Iterable[UnitInfo] = Iterable.empty
      val helperQueue = new mutable.PriorityQueue[UnitInfo]()(Ordering.by(framesToAttack(_, mine)))
      helperQueue ++= defuser.matchups.allies.filter(a => a.canMove && a.unitClass.attacksGround)
      while (damageRequired > 0 && helperQueue.nonEmpty) {
        val nextHelper = helperQueue.dequeue()
        damageRequired -= nextHelper.damageOnNextHitAgainst(mine)
        output = output ++ Iterable(nextHelper)
      }
      output
    }
  }

  override protected def perform(unit: FriendlyUnitInfo): Unit = {

    // If we haven't seen the mine in a long time,
    // maybe it blew up. Let's ignore it for now.
    if (With.framesSince(mine.lastSeen) > GameTime(0, 15)()) {
      return
    }

    // If we can kill the mine for free, let's do it
    if ( ! mine.effectivelyCloaked && unit.pixelRangeAgainst(mine) > 96) {
      attackMine(unit, mine)
    }

    // If we have helpers, let's wait until they're here
    val ourFramesToAttack = framesToAttack(unit, mine)
    val waitForHelpers = unit.pixelDistanceEdge(mine) < 96 + 16 && helpers.exists(h => framesToAttack(h, mine) > ourFramesToAttack + 4)
    if (waitForHelpers) {
      With.commander.hold(unit)
    }
    else if ( ! mine.effectivelyCloaked) {
      val radiansTo     = unit.pixelCenter.radiansTo(mine.pixelCenter)
      val radiansFacing = unit.angleRadians
      val radiansDiff   = Math.abs(PurpleMath.radiansTo(radiansFacing, radiansTo))
      if (mine.orderTarget.contains(unit)
        && radiansDiff < Math.PI / 8
        && ! unit.inRangeToAttack(mine)) {
        With.commander.hold(unit)
      }
      else {
        attackMine(unit, mine)
      }
    }
    else if (unit.matchups.targetsInRange.isEmpty) {
      unit.agent.toTravel = Some(mine.pixelCenter)
      Patrol.delegate(unit)
    }
  }

  def attackMine(attacker: FriendlyUnitInfo, mine: UnitInfo): Unit = {
    attacker.agent.toAttack = Some(mine)
    Attack.delegate(attacker)
  }

  def framesToAttack(attacker: UnitInfo, mine: UnitInfo): Double = {
    Math.max(
      attacker.cooldownLeft,
      (attacker.pixelDistanceEdge(mine) - attacker.pixelRangeAgainst(mine)
      / Math.max(1, attacker.topSpeed)))
  }
}
