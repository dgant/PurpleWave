package Micro.Agency

import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints, Tile}
import Mathematics.PurpleMath
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing.{TrafficPriorities, UnitLinearGroundPush}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import bwapi.UnitCommandType

// Commander is responsible for issuing unit commands
// in a way that Brood War handles gracefully,
// while taking advantage of Brood War mechanics to optimize commands.
//
// The goal is for the rest of the code base to be blissfully unaware
// of Brood War's glitchy unit behavior.
//
class Commander {
  def run(): Unit = {
    With.units.ours.foreach(unit => unit.sleepUntil(Math.max(unit.nextOrderFrame.getOrElse(0), AttackDelay.nextSafeOrderFrame(unit))))
  }
  
  def doNothing(unit: FriendlyUnitInfo): Unit = {
    if (unit.unready) return
    sleep(unit)
  }
  
  def stop(unit: FriendlyUnitInfo): Unit = {
    leadFollower(unit, stop)
    if (unit.unready) return
    unit.baseUnit.stop()
    sleep(unit)
  }
  
  def hold(unit: FriendlyUnitInfo): Unit = {
    leadFollower(unit, hold)
    if (unit.unready) return
    if ( ! unit.is(Zerg.Lurker)) autoUnburrow(unit)
    if (unit.velocity.lengthSquared > 0 && ! unit.holdingPosition) {
      unit.baseUnit.holdPosition()
      sleep(unit)
    }
    if (unit.matchups.targetsInRange.nonEmpty) {
      sleepAttack(unit)
    }
    else {
      unit.baseUnit.holdPosition()
      sleep(unit)
    }
  }

  private val tryingToMoveThreshold = 32

  def attack(unit: FriendlyUnitInfo): Unit = unit.agent.toAttack.foreach(attack(unit, _))
  private def attack(unit: FriendlyUnitInfo, target: UnitInfo): Unit = {
    leadFollower(unit, attack(_, target))
    unit.agent.directRide(target.pixelCenter)
    unit.agent.tryingToMove = unit.pixelsToGetInRange(target) > tryingToMoveThreshold
    if (unit.unready) return

    // Drop out of transport
    val dropship = unit.transport.find(_.isAny(Terran.Dropship, Protoss.Shuttle, Zerg.Overlord))
    val delay = unit.cooldownMaxAirGround
    if (dropship.isDefined
      && Math.min(unit.pixelDistanceEdge(target), unit.pixelDistanceEdge(target.projectFrames(delay)))
      <= unit.pixelRangeAgainst(target)
        + With.reaction.agencyAverage
        + delay * unit.topSpeed) {
      unload(dropship.get, unit)
      return
    }

    if ( ! unit.is(Zerg.Lurker)) autoUnburrow(unit)
    if ( ! unit.readyForAttackOrder) { sleep(unit); return }
  
    // TODO: Fix attack cancelling for Photon Cannons
    //
    // Via Ankmairdor, the iscript:
    // PhotonCannonGndAttkInit:
    //   playfram           2
    //   wait               2
    //   playfram           1
    //   wait               2
    //   playfram           3
    //   wait               2
    // PhotonCannonGndAttkRpt:
    //   wait               1
    //   attack
    //   gotorepeatattk
    //   ignorerest
    //
    // on first frame of attack, the animation runs twice
    // so there is 6 frame delay for first attack and 0 frame delay for continued attacks, also no non-interruptible section so additional commands can interupt an attack
    // though the biggest optimization would be issuing an attack command rather than waiting for them to autotarget
    // though I suppose with latency, this can backfire a small percentage(about 1/15 or 2/15) of the time and result in fewer attacks
    //
    // on average issuing an attack command that arrives on the first frame that a target is available will save 7 frames
    // if you wait until a target is in range to issue attack, you save an average of 3 frames (LF3)
    // if you waited to issue attack and the tower autotargets before your order arrives, you lose at least 22 frames(cooldown)
    // as far as I can tell, no penalty for bad early guesses on attacking
    if (unit.is(Protoss.PhotonCannon)) return

    if (unit.is(Zerg.Lurker) && ! unit.burrowed) { move(unit, target.pixelCenter); return }
    if (target.is(Protoss.Interceptor)) { attackMove(unit, target.pixelCenter); return }
    
    if (target.visible) {
      lazy val moving           = unit.moving
      lazy val alreadyInRange   = unit.inRangeToAttack(target)
      lazy val overdueToAttack  = unit.cooldownLeft == 0 && With.framesSince(unit.lastFrameStartingAttack) > 2.0 * unit.cooldownMaxAirGround
      lazy val thisIsANewTarget = ! unit.orderTarget.contains(target)
      
      val shouldOrder = (
        thisIsANewTarget
        || (overdueToAttack && (moving || alreadyInRange))
        || (target.isFriendly && unit.is(Protoss.Carrier))) // Carrier warmup; spam attack
      
      if (shouldOrder) {
        if ( ! unit.flying
          && ! unit.unitClass.floats
          && unit.matchups.targetsInRange.size == 1
          && unit.matchups.targetsInRange.head == target
          // Because Reavers use BW's internal ground distance we can't be sure the target is in range
          && ! unit.is(Protoss.Reaver)) {
          unit.baseUnit.holdPosition()
        } else {
          unit.baseUnit.attack(target.baseUnit)
        }
      }
      sleepAttack(unit)
    } else {
      move(unit, target.pixelCenter)
    }
  }

  /**
    * Adjust the unit's destination to ensure successful execution of the move.
    */
  def getAdjustedDestination(unit: FriendlyUnitInfo, argTo: Pixel): Pixel = {
    var to: Pixel = argTo

    // Send some units past their destination to maximize acceleration
    val overshootDistance = if (unit.flying) 288.0 else 8
    if (unit.isAny(Terran.Dropship, Terran.ScienceVessel, Protoss.Shuttle, Protoss.Observer, Protoss.HighTemplar, Zerg.Mutalisk, Zerg.Overlord, Zerg.Queen)) {
      if (to == unit.pixelCenter) {
        val signX = PurpleMath.forcedSignum(SpecificPoints.middle.x - to.x)
        val signY = PurpleMath.forcedSignum(SpecificPoints.middle.y - to.y)
        to = to.add((signX * overshootDistance).toInt, (signY * overshootDistance).toInt)
      } else if(unit.pixelDistanceSquared(to) < overshootDistance * overshootDistance) {
        to = unit.pixelCenter.project(to, overshootDistance)
      }
    }

    // Clip to map
    to = Pixel(
      PurpleMath.clamp(to.x, unit.unitClass.dimensionLeft, With.mapPixelWidth  - unit.unitClass.dimensionRight),
      PurpleMath.clamp(to.y, unit.unitClass.dimensionUp,   With.mapPixelHeight - unit.unitClass.dimensionDown))

    // Path around terrain (if we haven't already)
    if ( ! unit.flying && unit.pixelDistanceTravelling(to) >= 2 * MicroPathing.waypointDistancePixels && With.reaction.agencyAverage < 6 && unit.zone != to.zone) {
      to = MicroPathing.getWaypointToPixel(unit, to)
    }

    // Cleave to walkable terrain until we're arriving at the destination
    // This could prevent getting stuck eg when trying to move behind a mineral line with a Dragoon
    else if ( ! unit.flying && ! to.tileIncluding.walkable && unit.pixelDistanceCenter(to) > 32) {
      to = to.nearestWalkableTile.pixelCenter
    }

    // Apply noise
    //
    // According to https://github.com/tscmoo/tsc-bwai/commit/ceb13344f5994d28d6b601cef126f264ca97426b
    // ordering moves to the exact same destination causes Brood War to not recalculate the path.
    //
    // That means that if the unit got confused while executing the original order, it will remain confused.
    // Issuing an order to a slightly different position can cause Brood War to recalculate the path and un-stick the unit.
    //
    // However, this recalculation can itself sometimes cause units to get stuck on obstacles.
    // Specifically, units tend to get stuck on buildings this way.
    // The neutral buildings on Roadrunner frequently cause this.
    //
    // So we'll try to get the best of both worlds, and recalculate paths *occasionally*
    // and only at distances long enough to avoid the issue.
    //
    // Also, give different units different paths to avoid "conga line" behavior
    //
    if (unit.pixelDistanceCenter(to) > 160) {
      to = to.add((unit.id + With.frame / 24) % 3 - 1, 0)
    }
    to
  }

  def attackMove(unit: FriendlyUnitInfo): Unit = unit.agent.toTravel.foreach(attackMove(unit, _))
  private def attackMove(unit: FriendlyUnitInfo, destination: Pixel) {
    leadFollower(unit, attackMove(_, destination))
    unit.agent.directRide(destination)
    unit.agent.tryingToMove = unit.pixelDistanceCenter(destination) > tryingToMoveThreshold
    if (unit.unready) return
    val to = getAdjustedDestination(unit, destination)
    autoUnburrowUnlessLurkerInRangeOf(unit, to)
    val alreadyAttackMovingThere = unit.command.exists(c => c.getType == UnitCommandType.Attack_Move && new Pixel(c.getTargetPosition).pixelDistance(to) < 128)
    if ( ! alreadyAttackMovingThere || unit.seeminglyStuck) {
      unit.baseUnit.attack(to.bwapi)
    }
    sleepAttack(unit)
  }

  def patrol(unit: FriendlyUnitInfo): Unit = unit.agent.toTravel.foreach(patrol(unit, _))
  private def patrol(unit: FriendlyUnitInfo, destination: Pixel) {
    leadFollower(unit, patrol(_, destination))
    unit.agent.directRide(destination)
    unit.agent.tryingToMove = unit.pixelDistanceCenter(destination) > tryingToMoveThreshold
    if (unit.unready) return
    val to = getAdjustedDestination(unit, destination)
    autoUnburrowUnlessLurkerInRangeOf(unit, to)
    unit.baseUnit.patrol(to.bwapi)
    sleepAttack(unit)
  }

  def move(unit: FriendlyUnitInfo): Unit = unit.agent.toTravel.foreach(move(unit, _))
  private def move(unit: FriendlyUnitInfo, destination: Pixel) {
    leadFollower(unit, move(_, destination))
    unit.agent.directRide(destination)
    unit.agent.tryingToMove = unit.pixelDistanceCenter(destination) > tryingToMoveThreshold
    if (unit.unready) return
    autoUnburrow(unit)
    val to = getAdjustedDestination(unit, destination)
    if (unit.is(Terran.Medic) && unit.agent.shouldEngage) {
      attackMove(unit, to)
    } else if (unit.pixelDistanceCenter(to) > 3) {
      // When bot is slowing down, use attack-move
      if (unit.agent.shouldEngage
        && With.reaction.agencyAverage > 12
        && ! unit.unitClass.isWorker
        && unit.canAttack) {
        attackMove(unit, to)
      } else if (
        // If we have a ride which can get us there faster, take it
        unit.agent.ride.isDefined
          && unit.framesToTravelTo(destination) >
          4 * unit.unitClass.groundDamageCooldown
            + unit.agent.ride.get.framesToTravelTo(unit.pixelCenter)
            + unit.agent.ride.get.framesToTravelPixels(unit.pixelDistanceCenter(destination))) {
        rightClick(unit, unit.agent.ride.get)
      }
      else {

        unit.baseUnit.move(destination.bwapi)
      }
      if (unit.agent.priority > TrafficPriorities.None) {
        With.coordinator.pushes.put(new UnitLinearGroundPush(
          unit.agent.priority,
          unit,
          unit.pixelCenter.project(destination, Math.min(unit.pixelDistanceCenter(destination), 128))))
      }
    }

    sleep(unit)
  }
  
  def rightClick(unit: FriendlyUnitInfo, target: UnitInfo) {
    leadFollower(unit, rightClick(_, target))
    unit.agent.directRide(target.pixelCenter)
    if (unit.unready) return
    if ( ! unit.is(Zerg.Lurker)) autoUnburrow(unit)
    unit.baseUnit.rightClick(target.baseUnit)
    sleepAttack(unit)
  }
  
  def useTech(unit: FriendlyUnitInfo, tech: Tech) {
    if (unit.unready) return
    autoUnburrow(unit)
    if (tech == Terran.Stim) {
      if (With.framesSince(unit.agent.lastStim) < 24) return
      unit.agent.lastStim = With.frame
    }
    unit.baseUnit.useTech(tech.baseType)
    sleep(unit)
  }
  
  def useTechOnUnit(unit: FriendlyUnitInfo, tech: Tech, target: UnitInfo) {
    if (unit.unready) return
    unit.agent.directRide(target.pixelCenter)
    autoUnburrow(unit)
    unit.baseUnit.useTech(tech.baseType, target.baseUnit)
    if (tech == Protoss.ArchonMeld || tech == Protoss.DarkArchonMeld) {
      sleep(unit, 48)
    }
    else {
      sleep(unit)
    }
  }
  
  def useTechOnPixel(unit: FriendlyUnitInfo, tech: Tech, target: Pixel) {
    if (unit.unready) return
    autoUnburrow(unit)
    unit.baseUnit.useTech(tech.baseType, target.bwapi)
    if (tech == Terran.SpiderMinePlant) {
      sleep(unit, 12)
    } else {
      sleepAttack(unit)
    }
  }
  
  def repair(unit: FriendlyUnitInfo, target: UnitInfo) {
    if (unit.unready) return
    unit.baseUnit.repair(target.baseUnit)
    sleep(unit, 24)
  }
  
  def returnCargo(unit: FriendlyUnitInfo): Unit = {
    if (unit.unready) return
    if (unit.carryingResources) {
      autoUnburrow(unit)
      unit.baseUnit.returnCargo
      sleepReturnCargo(unit)
    }
  }

  def gather(unit: FriendlyUnitInfo): Unit = unit.agent.toGather.foreach(gather(unit, _))
  private def gather(unit: FriendlyUnitInfo, resource: UnitInfo, allowReturningCargo: Boolean = true) {
    if (unit.unready) return
    autoUnburrow(unit)
    if (allowReturningCargo && (unit.carryingMinerals || unit.carryingGas)) {
      if ( ! unit.gatheringGas && ! unit.gatheringMinerals) {
        returnCargo(unit)
      }
      else {
        sleep(unit)
      }
    }
      
    // The logic of "If we're not carrying resources, spam gather until the unit's target is the intended resource"
    // produces mineral locking, in which workers mine more efficiently because exactly 2 miners saturate a mineral patch.
    //
    else if ( ! unit.target.contains(resource)) {
      if (resource.visible) {
        unit.baseUnit.gather(resource.baseUnit)
        sleep(unit)
      }
      else {
        move(unit, resource.pixelCenter)
      }
    }
    else {
      sleep(unit)
    }
  }

  def build(unit: FriendlyUnitInfo, unitClass: UnitClass) {
    if (unit.unready) return
    autoUnburrow(unit)
    unit.baseUnit.build(unitClass.baseType)
    sleepBuild(unit)
  }
  
  def build(unit: FriendlyUnitInfo, unitClass: UnitClass, tile: Tile) {
    if (unit.unready) return
    autoUnburrow(unit)
    if (unit.pixelDistanceSquared(tile.pixelCenter) > Math.pow(32.0 * 5.0, 2)) {
      move(unit, tile.pixelCenter)
      return
    }
    unit.baseUnit.build(unitClass.baseType, tile.bwapi)
    sleepBuild(unit)
  }
  
  def tech(unit: FriendlyUnitInfo, tech: Tech) {
    if (unit.unready) return
    unit.baseUnit.research(tech.baseType)
    sleep(unit)
  }
  
  def upgrade(unit: FriendlyUnitInfo, upgrade: Upgrade) {
    if (unit.unready) return
    unit.baseUnit.upgrade(upgrade.baseType)
    sleep(unit)
  }
  
  def cancel(unit: FriendlyUnitInfo) {
    if (unit.unready) return
    if (unit.teching) {
      unit.baseUnit.cancelResearch()
    } else if (unit.upgrading) {
      unit.baseUnit.cancelUpgrade()
    } else {
      unit.baseUnit.cancelConstruction()
    }
    sleep(unit)
  }
  
  def rally(unit: FriendlyUnitInfo, pixel: Pixel) {
    if (unit.unready) return
    unit.baseUnit.setRallyPoint(pixel.bwapi)
    unit.lastSetRally = With.frame
    sleep(unit)
  }
  
  def rally(unit: FriendlyUnitInfo, targetUnit: UnitInfo) {
    if (unit.unready) return
    unit.baseUnit.setRallyPoint(unit.baseUnit)
    unit.lastSetRally = With.frame
    sleep(unit)
  }
  
  
  def unload(transport: FriendlyUnitInfo, passenger: UnitInfo) {
    // No sleeping required
    transport.baseUnit.unload(passenger.baseUnit)
  }
  
  def addon(unit: FriendlyUnitInfo, unitClass: UnitClass) {
    if (unit.unready) return
    unit.baseUnit.buildAddon(unitClass.baseType)
    sleep(unit)
  }
  
  def buildScarab(unit: FriendlyUnitInfo) {
    if (unit.unready) return
    unit.baseUnit.build(Protoss.Scarab.baseType)
    sleep(unit)
  }
  
  def buildInterceptor(unit: FriendlyUnitInfo) {
    if (unit.unready) return
    unit.baseUnit.build(Protoss.Interceptor.baseType)
    sleep(unit)
  }
  
  def cloak(unit: FriendlyUnitInfo, tech: Tech) {
    leadFollower(unit, cloak(_, tech))
    if (unit.unready) return
    unit.agent.lastCloak = With.frame
    unit.baseUnit.cloak()
    sleep(unit)
  }
  
  def decloak(unit: FriendlyUnitInfo, tech: Tech) {
    leadFollower(unit, decloak(_, tech))
    if (unit.unready) return
    unit.baseUnit.decloak()
    sleep(unit)
  }
  
  def burrow(unit: FriendlyUnitInfo) {
    if (unit.unready) return
    unit.baseUnit.burrow()
    sleep(unit)
  }
  
  def unburrow(unit: FriendlyUnitInfo) {
    if (unit.unready) return
    unit.baseUnit.unburrow()
    sleep(unit)
  }
  
  def lift(unit: FriendlyUnitInfo) {
    if (unit.unready) return
    unit.baseUnit.lift()
    sleep(unit)
  }
  
  private def autoUnburrow(unit: FriendlyUnitInfo): Unit = {
    if (unit.unready) return
    if (unit.burrowed) unburrow(unit)
  }

  private def autoUnburrowUnlessLurkerInRangeOf(unit: FriendlyUnitInfo, to: Pixel): Unit = {
    if ( ! unit.is(Zerg.Lurker) || unit.pixelDistanceCenter(to) > unit.pixelRangeGround) autoUnburrow(unit)
  }
  
  private def sleepAttack(unit: FriendlyUnitInfo) {
    sleep(unit, AttackDelay.framesToWaitAfterIssuingAttackOrder(unit))
  }
  
  private def sleepBuild(unit: FriendlyUnitInfo) {
    // Based on https://github.com/tscmoo/tsc-bwai/blame/master/src/unit_controls.h#L1497
    sleep(unit, 1 + 2 * With.latency.latencyFrames)
  }
  
  private def sleepReturnCargo(unit:FriendlyUnitInfo) {
    // Based on https://github.com/tscmoo/tsc-bwai/blame/master/src/unit_controls.h#L1442
    sleep(unit, 8)
  }
  
  private def sleep(unit: FriendlyUnitInfo, requiredDelay: Int = 2): Unit = {
    val sleepUntil = Array(
      With.frame + With.configuration.performanceMinimumUnitSleep,
      With.frame + requiredDelay,
      With.frame + With.latency.turnSize,
      unit.nextOrderFrame.getOrElse(0)).max
    unit.sleepUntil(sleepUntil)
  }

  private def leadFollower(unit: FriendlyUnitInfo, todo: (FriendlyUnitInfo) => Unit): Unit = {
    if (unit.ready) {
      unit.agent.leadFollower = todo
    }
  }
}
