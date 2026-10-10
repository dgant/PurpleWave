package Micro.Actions.Protoss

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Agency.{Commander, ReaverCommander}
import Micro.Targeting.FiltersSituational.{TargetFilterPotshot, TargetFilterVisibleInRange}
import Micro.Targeting.Target
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object ReaverShot {
  // Attack becomes ReaverAttack, then ReaverFight. Arriving much earlier can put the
  // cooldown expiry just after a ReaverFight check, costing another nine-frame poll.
  val attackLeadFrames = 2
  val aimDistanceMax = 32

  // Keep the waypoint ahead through command delivery, with an accurate heading.
  // Stop as soon as we face the target; the waypoint itself is at most one tile away.
  def aimPoint(from: Pixel, target: Pixel): Pixel = {
    val distance = from.pixelDistance(target)
    if (distance <= 16) return target
    val dx = target.x - from.x
    val dy = target.y - from.y
    var best = from
    var bestError = Double.PositiveInfinity
    for (step <- 16 to aimDistanceMax) {
      val to = from.add(Math.round(dx * step / distance).toInt, Math.round(dy * step / distance).toInt)
      val x = to.x - from.x
      val y = to.y - from.y
      val error = Math.abs(Math.atan2(dx.toDouble * y - dy.toDouble * x, dx.toDouble * x + dy.toDouble * y))
      if (error < bestError) { best = to; bestError = error }
      if (error <= Math.PI / 256) return to
    }
    best
  }

  def aimPoint(from: Pixel, target: Pixel, origin: Pixel): Option[Pixel] =
    Some(aimPoint(from, target)).filter(_.pixelDistance(origin) <= aimDistanceMax + 1)

  def preparationFrames(latency: Int, reaction: Int, turn: Int): Int =
    latency + Math.max(1, reaction) + turn + 2 + attackLeadFrames
}

// One plan per Reaver. Lock the target only during preparation, so target scoring
// cannot turn us away at the deadline; release it after the shot or if it becomes illegal.
final class ReaverShot(unit: FriendlyUnitInfo) {
  private var preparing: Option[UnitInfo] = None
  private var aimOrigin: Pixel = unit.pixel
  private var aimDestination: Option[Pixel] = None
  private var aimDeliveredBy: Int = 0
  private var attackIssued: Boolean = false
  private var attackFrame: Int = 0
  private var lastPlanningFrame: Int = -1000000

  def reset(): Unit = {
    preparing = None
    aimDestination = None
    aimDeliveredBy = 0
    attackIssued = false
  }

  def perform(retreating: Boolean): Boolean = {
    if (unit.unready) return true
    if (unit.loaded) { reset(); return false }

    val lead = ReaverShot.preparationFrames(With.latency.remainingFrames, With.reaction.agencyMax, Protoss.Reaver.framesToTurn180)
    if (unit.cooldownLeft > lead || (retreating && unit.hasActiveScarab)) {
      reset()
      return false // Existing combat movement and retreat own the cooldown interval.
    }

    def legal(target: UnitInfo): Boolean = (
      target.alive && target.visible && unit.canAttack(target) && unit.inRangeToAttack(target)
        && unit.hasScarabPathTo(target, unit.pixel)
        && ( ! retreating || TargetFilterPotshot.legal(unit, target)))

    if (With.framesSince(lastPlanningFrame) > lead || preparing.exists(t => ! legal(t))) reset()
    lastPlanningFrame = With.frame
    val selected = preparing.filter(legal)
      .orElse(unit.agent.toAttack.filter(legal))
      .orElse(Target.best(unit, TargetFilterVisibleInRange).filter(legal))
    if (selected.isEmpty) { reset(); return false }
    val target = selected.get
    unit.agent.toAttack = selected

    if (preparing.isEmpty) {
      preparing = selected
      aimOrigin = unit.pixel
    }
    if (attackIssued) {
      // Observe command delivery and allow one complete native processing interval.
      // Do not reset ReaverAttack/ReaverFight on every agency update.
      if (With.framesSince(attackFrame) < With.latency.latencyFrames + 12) {
        Commander.sleep(unit, 1)
        return true
      }
      attackIssued = false
    }
    if (unit.scarabs > 0 && ! unit.hasActiveScarab && unit.cooldownLeft <= With.latency.remainingFrames + ReaverShot.attackLeadFrames) {
      unit.agent.act("ReaverFire")
      attackIssued = ReaverCommander.attack(unit, target)
      attackFrame = With.frame
    } else {
      unit.agent.act("ReaverAim")
      // Observe delivery before replacing an aiming waypoint.
      // Otherwise latency can queue conflicting short moves around the same spot.
      if (With.frame < aimDeliveredBy) { Commander.sleep(unit, 1); return true }
      // Lead a moving target to cooldown expiry; command latency otherwise makes
      // us aim at where it was, even if our own turn finished on time.
      val aimFrames = Math.max(unit.cooldownLeft, With.latency.remainingFrames + ReaverShot.attackLeadFrames)
      val aimTarget = target.pixel.add(Math.round(target.velocityX * aimFrames).toInt, Math.round(target.velocityY * aimFrames).toInt)
      val heading = unit.pixel.radiansToSlow(aimTarget)
      val facing = Math.abs(Maff.radiansTo(unit.angleRadians, heading)) <= Math.PI / 128
      if (facing) {
        ReaverCommander.hold(unit)
      } else {
        // Retain the same short move while it still aims at the target. If the target
        // moves sideways, turn along a new ray from HERE, not back to the old anchor.
        // Bound all such adjustments around the original position, including while
        // waiting for an active Scarab, so updates cannot produce an endless crawl.
        val previous = aimDestination.filter(p => p != unit.pixel && Math.abs(Maff.radiansTo(unit.pixel.radiansToSlow(p), heading)) <= Math.PI / 128)
        val next = previous.orElse(ReaverShot.aimPoint(unit.pixel, aimTarget, aimOrigin))
          .filter(p => p.traversableBy(unit) && unit.inRangeToAttackFrom(target, p) && unit.hasScarabPathTo(target, p))
        next match {
          case Some(to) =>
            if ( ! aimDestination.contains(to)) aimDeliveredBy = With.frame + With.latency.remainingFrames + 1
            aimDestination = Some(to)
            ReaverCommander.move(unit, to)
          case None => ReaverCommander.hold(unit)
        }
      }
    }
    true
  }
}
