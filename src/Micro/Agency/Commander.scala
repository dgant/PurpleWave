package Micro.Agency

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Points, Tile}
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing._
import ProxyBwapi.Orders
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import ProxyBwapi.Upgrades.Upgrade
import Utilities.?

// Commander is responsible for issuing unit commands
// in a way that Brood War handles gracefully,
// while taking advantage of Brood War mechanics to optimize commands.
//
// The goal is for the rest of the code base to be blissfully unaware
// of Brood War's glitchy unit behavior.
//
object Commander {

  private val tryingToMoveThreshold = 32
  
  def doNothing(unit: FriendlyUnitInfo): Unit = {
    if (unit.unready) return
    sleep(unit)
  }
  
  def stop(unit: FriendlyUnitInfo): Unit = {
    unit.agent.setRideGoal(unit.pixel)
    leadFollower(unit, stop)
    if (unit.unready) return
    unit.bwapiUnit.stop()
    sleep(unit)
    unit.resetSticking()
  }
  
  def hold(unit: FriendlyUnitInfo): Unit = {
    unit.agent.setRideGoal(unit.pixel)
    leadFollower(unit, hold)
    if (unit.unready) return
    if ( ! Zerg.Lurker(unit) && autoUnburrow(unit)) return
    if (unit.velocity.lengthSquared > 0 && unit.order != Orders.HoldPosition) {
      unit.bwapiUnit.holdPosition()
    }
    if (unit.matchups.targetsInRange.nonEmpty) {
      sleepAttack(unit)
    } else {
      sleep(unit)
    }
  }

  // We used to refuse to issue commands to Photon Cannons due to inadvertent attack cancelling.
  // It looks like we've fixed that by using a 6-frame stopFrame
  //
  // Via Ankmairdor, the iscript for Photon Cannon attacks:
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

  private def acceptableAttackFrom(unit: FriendlyUnitInfo, target: UnitInfo, attackFrom: Pixel): Boolean = {
    if ( ! target.visible && target.pixelDistanceCenter(attackFrom) > 16)                         false
    else if (unit.inRangeToAttackFrom(target, attackFrom))                                        true
    else if (unit.pixelsToGetInRange(target) <= unit.pixelsToGetInRangeFrom(target, attackFrom))  false
    else                                                                                          true
  }

  def attack(unit: FriendlyUnitInfo): Unit = unit.agent.toAttack.foreach(attack(unit, _))
  private def attack(unit: FriendlyUnitInfo, target: UnitInfo): Unit = {
    unit.agent.toAttack = Some(target)
    lazy val attackFrom = unit.agent.perch.pixel.filter(acceptableAttackFrom(unit, target, _)).getOrElse(unit.agent.choosePerch()())

    if (Protoss.Reaver(unit)) {
      if (unit.loaded)  pushAway    (unit, attackFrom, TrafficPriorities.Bump)
      else              pushThrough (unit, attackFrom, TrafficPriorities.Bump)
    } else if ( ! target.visible || ! unit.inRangeToAttack(target)) {
      pushThrough(unit, attackFrom, TrafficPriorities.Nudge)
    }

    unit.agent.setRideGoal(attackFrom)
    leadFollower(unit, attack(_, target))
    unit.agent.tryingToMove = unit.pixelsToGetInRange(target) > tryingToMoveThreshold
    if (unit.unready) return

    if (unit.airlifted && (unit.pixelDistanceCenter(attackFrom) < ?(unit.agent.isPrimaryPassenger, 32, 64) || unit.pixelsToGetInRange(target) <= ?(unit.agent.isPrimaryPassenger, 16, 48))) {
      requestUnload(unit);
      sleep(unit) //  Although we're not doing anything, it's important to sleep here so we don't keep executing and change our ride goal
      return
    }
    if ( ! Zerg.Lurker(unit) && autoUnburrow(unit)) return

    // We shouldn't target interceptors anymore, but in case we do:
    if (Protoss.Interceptor(target)) {
      attackMove(unit, attackFrom); return
    }

    // When a ground unit attacks a Carrier, have it attack-move when off cooldown to ensure we burn off Interceptors even if we can't find a route to the Carrier
    if (Protoss.Carrier(target) && unit.readyForAttackOrder && ! unit.inRangeToAttack(target) && ! unit.flying) {
      attackMove(unit, attackFrom); return
    }

    if (Zerg.Lurker(unit) && ! unit.burrowed) {
      move(unit, attackFrom); return
    }

    // If we should be attack-commanding
    var useAttackCommand    = unit.readyForAttackOrder || unit.inRangeToAttack(target) // Allow deliberate repositioning between shots
    useAttackCommand      ||= unit.unitClass.melee
    useAttackCommand      ||= ! unit.canMove
    useAttackCommand      ||= unit.flying && unit.readyForAttackOrder // Flying units can attack directly but if repositioning between shots need a move command
    useAttackCommand      ||= unit.framesToGetInRange(target) <= With.latency.latencyFrames + Math.max(unit.cooldownLeft, With.reaction.agencyAverage)
    useAttackCommand      &&= target.visible
    if (useAttackCommand) {
      lazy val moving           = unit.moving
      lazy val alreadyInRange   = unit.inRangeToAttack(target)
      lazy val overdueToAttack  = unit.cooldownLeft == 0 && With.framesSince(unit.lastFrameStartingAttack) > 2.0 * unit.cooldownMaxAirGround
      lazy val thisIsANewTarget = ! unit.orderTarget.contains(target)
      val shouldOrder = (
        thisIsANewTarget
          || (overdueToAttack && (moving || alreadyInRange))
          || (target.isFriendly && Protoss.Carrier(unit))) // Carrier warmup; spam attack to avoid actually doing damage

      if (shouldOrder) {
        unit.bwapiUnit.attack(target.bwapiUnit)
        target.addFutureAttack(unit)
        sleepAttack(unit)
      } else {
        sleep(unit)
      }
      return
    }
    move(unit, attackFrom)
  }

  /**
    * Adjust the unit's destination to ensure successful execution of the move.
    */
  def adjustDestination(unit: FriendlyUnitInfo, argTo: Pixel): Pixel = {
    var to: Pixel = argTo

    // Send some units past their destination to maximize acceleration
    if (With.reaction.sluggishness == 0 && (unit.isAny(Terran.ScienceVessel, Protoss.Observer, Zerg.Mutalisk, Zerg.Overlord, Zerg.Queen) || unit.loadedUnits.nonEmpty)) {
      val overshootDistance = ?(unit.airborne, 288.0, 8)
      if (to == unit.pixel) {
        val signX = Maff.signum11(Points.middle.x - to.x)
        val signY = Maff.signum11(Points.middle.y - to.y)
        to = to.add((signX * overshootDistance).toInt, (signY * overshootDistance).toInt)
      } else if (unit.pixelDistanceSquared(to) < overshootDistance * overshootDistance) {
        to = unit.pixel.project(to, overshootDistance)
      }
    }

    // Clip to map
    to = Pixel(
      Maff.clamp(to.x, unit.unitClass.dimensionLeft, With.mapPixelWidth  - unit.unitClass.dimensionRightInclusive),
      Maff.clamp(to.y, unit.unitClass.dimensionUp,   With.mapPixelHeight - unit.unitClass.dimensionDownInclusive))

    if (unit.airborne) return to

    // Path around terrain (if we haven't already)
    if (unit.pixelDistanceTravelling(to) >= 2 * MicroPathing.waypointDistancePixels && With.reaction.sluggishness < 2 && unit.zone != to.zone) {
      to = MicroPathing.getWaypointToPixel(unit, to)
    }

    // Even slight intersection with a non-walkable tile can cause a unit to refuse movement
    // This could prevent getting stuck eg when trying to move behind a mineral line with a Dragoon
    var walkabilityAssured = to.walkable
    walkabilityAssured &&= to.add( - unit.unitClass.dimensionLeft,            - unit.unitClass.dimensionUp            ).walkable
    walkabilityAssured &&= to.add(   unit.unitClass.dimensionRightInclusive,    unit.unitClass.dimensionDownInclusive ).walkable
    walkabilityAssured &&= to.add(   unit.unitClass.dimensionRightInclusive,  - unit.unitClass.dimensionUp            ).walkable
    walkabilityAssured &&= to.add( - unit.unitClass.dimensionLeft,              unit.unitClass.dimensionDownInclusive ).walkable
    if ( ! walkabilityAssured && unit.pixelDistanceCenter(to) > 32) {
      to = to.walkableTile.center
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
    if (unit.pixelDistanceCenter(to) > 160) {
      to = to.add((unit.id + With.frame / 96) % 3 - 1, 0)
    }
    to
  }

  def attackMove(unit: FriendlyUnitInfo): Unit = attackMove(unit, unit.agent.destinationNext())
  private def attackMove(unit: FriendlyUnitInfo, destination: Pixel): Unit = {
    unit.agent.setRideGoal(destination)
    leadFollower(unit, attackMove(_, destination))
    unit.agent.tryingToMove = unit.pixelDistanceCenter(destination) > tryingToMoveThreshold
    if (unit.unready) return
    val to = adjustDestination(unit, destination)
    autoUnburrowUnlessLurkerInRangeOf(unit, to)
    val alreadyAttackMovingThere = unit.order == Orders.AttackMove && unit.orderTargetPixel.exists(_.pixelDistance(to) < 128)
    if ( ! alreadyAttackMovingThere || unit.seeminglyStuck) {
      unit.bwapiUnit.attack(to.bwapi)
    }
    sleepAttack(unit)
  }

  def patrol(unit: FriendlyUnitInfo): Unit = patrol(unit, unit.agent.destinationNext())
  private def patrol(unit: FriendlyUnitInfo, destination: Pixel): Unit = {
    unit.agent.setRideGoal(destination)
    leadFollower(unit, patrol(_, destination))
    unit.agent.tryingToMove = unit.pixelDistanceCenter(destination) > tryingToMoveThreshold
    if (unit.unready) return
    val to = adjustDestination(unit, destination)
    autoUnburrowUnlessLurkerInRangeOf(unit, to)
    unit.bwapiUnit.patrol(to.bwapi)
    sleepAttack(unit)
  }

  def move(unit: FriendlyUnitInfo): Unit = move(unit, unit.agent.destinationNext())
  private def move(unit: FriendlyUnitInfo, destination: Pixel): Unit = {
    val to = adjustDestination(unit, destination)
    unit.agent.setRideGoal(to)
    leadFollower(unit, move(_, to))
    unit.agent.tryingToMove = unit.pixelDistanceCenter(to) > tryingToMoveThreshold
    if (unit.unready) return
    if (autoUnburrow(unit)) return
    if (unit.pixelDistanceCenter(to) <= 8) {
      requestUnload(unit)
    }

    if (Terran.Medic(unit) && unit.agent.shouldFight) {
      attackMove(unit, to)
    } else if (unit.pixelDistanceCenter(to) > 3) {
      // When bot is sluggish, use attack-move
      if (  ! unit.unitClass.isWorker
        && unit.agent.shouldFight
        && With.reaction.sluggishness >= 3
        && unit.canAttack) {
        attackMove(unit, to)
      } else {
        unit.bwapiUnit.move(to.bwapi)
      }
      if ( ! unit.flying) {
        pushThrough(unit, to, TrafficPriorities.None)
      }
    }

    sleep(unit)
  }

  def rightClick(unit: FriendlyUnitInfo, target: UnitInfo): Unit = {
    leadFollower(unit, rightClick(_, target))
    unit.agent.tryingToMove = unit.pixelDistanceEdge(target) > tryingToMoveThreshold
    if (unit.agent.ride.contains(target)) {
      unit.agent.wantsPickup = true
    } else {
      unit.agent.setRideGoal(target.pixel)
    }
    if (unit.unready) return
    if ( ! Zerg.Lurker(unit) && autoUnburrow(unit)) return
    unit.bwapiUnit.rightClick(target.bwapiUnit)
    sleepAttack(unit)
  }

  def useTech(unit: FriendlyUnitInfo, tech: Tech): Unit = {
    if (tech == Terran.Stim) {
      if (With.framesSince(unit.agent.lastStim) < 24) return
      unit.agent.lastStim = With.frame
    }
    if (unit.unready)       return
    if (autoUnburrow(unit)) return
    if (autoUnload(unit))   return
    unit.bwapiUnit.useTech(tech.bwapiTech)
    sleep(unit)
  }

  def useTechOnUnit(unit: FriendlyUnitInfo, tech: Tech, target: UnitInfo): Unit = {
    unit.agent.setRideGoal(target.pixel)
    if (unit.unready)       return
    if (autoUnburrow(unit)) return
    if (autoUnload(unit))   return
    unit.bwapiUnit.useTech(tech.bwapiTech, target.bwapiUnit)
    if (tech == Protoss.ArchonMeld || tech == Protoss.DarkArchonMeld) {
      sleep(unit, 48)
    } else {
      sleep(unit)
    }
  }

  def useTechOnPixel(unit: FriendlyUnitInfo, tech: Tech, target: Pixel): Unit = {
    unit.agent.setRideGoal(target)
    if (unit.unready)       return
    if (autoUnburrow(unit)) return
    if (autoUnload(unit))   return
    unit.bwapiUnit.useTech(tech.bwapiTech, target.bwapi)
    if (tech == Terran.SpiderMinePlant) {
      sleep(unit, 12)
    } else {
      sleepAttack(unit)
    }
  }

  def repair(unit: FriendlyUnitInfo, target: UnitInfo): Unit = {
    unit.agent.setRideGoal(target.pixel)
    if (unit.unready)       return
    if (autoUnburrow(unit)) return
    if (autoUnload(unit))   return
    sleep(unit, 24)
  }

  def returnCargo(unit: FriendlyUnitInfo): Unit = {
    if (unit.unready)       return
    if (autoUnburrow(unit)) return
    if (autoUnload(unit))   return
    if (unit.carrying) {
      unit.bwapiUnit.returnCargo()
      sleepReturnCargo(unit)
    }
  }

  def gather(unit: FriendlyUnitInfo): Unit = unit.agent.toGather.foreach(gather(unit, _))
  private def gather(unit: FriendlyUnitInfo, resource: UnitInfo): Unit = {
    unit.agent.setRideGoal(resource.pixel)
    if (unit.unready)       return
    if (autoUnburrow(unit)) return
    if (autoUnload(unit))   return
    if (unit.carrying) {
      // Spamming return cargo can cause path wobbling
      if ( ! Vector(Orders.ResetCollision, Orders.ReturnMinerals, Orders.ReturnGas).contains(unit.order)) {
        val incompleteHall = unit.base.flatMap(_.townHall.filterNot(_.complete))
        lazy val nextClosestFrames = Maff.min(With.geography.ourBases.view.flatMap(_.townHall).filter(_.complete).map(h => unit.pixelDistanceTravelling(h.exitTile) / unit.topSpeed))
        if (incompleteHall.exists(hall => nextClosestFrames.forall(_ * 2 > hall.remainingCompletionFrames))) {
          move(unit, incompleteHall.get.pixel)
        } else {
          unit.bwapiUnit.returnCargo()
        }
      }
    } else {
      if (resource.visible) {
        def doGather(): Unit = {
          unit.bwapiUnit.rightClick(resource.bwapiUnit)
        }
        if (resource.unitClass.isGas) {
          if ( ! unit.orderTarget.contains(resource)) {
            doGather()
          }
        } else {
          lazy val coworkers            = With.gathering.getWorkersByResource(resource)
          lazy val accelerantFrame      = 11 + With.latency.remainingFrames
          lazy val accelerantMineral    = With.gathering.getAccelerantMineral(resource)
          lazy val accelerantPixel      = With.gathering.getAccelerantPixelSteady(resource)
          lazy val distance             = unit.pixelDistanceEdge(resource)
          lazy val projectedFrames      = Maff.nanToZero(Math.max(0, distance - unit.unitClass.haltPixels) / unit.topSpeed + 2 * Math.min(distance, unit.unitClass.haltPixels) / unit.topSpeed)
          lazy val onAccelerantPixel    = With.gathering.onAccelerant(unit, resource)
          lazy val onTargetMineral      = unit.orderTarget.contains(resource)
          lazy val onAccelerantMineral  = accelerantMineral.exists(unit.orderTarget.contains)
          def doGatherFromAccelerant(): Unit = { unit.bwapiUnit.gather(accelerantMineral.get.bwapiUnit) }
          if (unit.order == Orders.MiningMinerals) {
            // Leave well alone!
          } else if (onAccelerantPixel) {
            doGather()
          // See https://github.com/bmnielsen/Stardust/blob/b8a91e52f453e6fdc60798edac569826df98148a/src/Workers/Workers.cpp#L587
          } else if (coworkers.exists(w => w != unit && w.order == Orders.MiningMinerals && w.orderTarget.contains(resource) && w.bwapiUnit.getOrderTimer + 7 == 11 + With.latency.remainingFrames)) {
            doGather()
          } else if (projectedFrames == accelerantFrame) {
            doGather()
          } else if (accelerantMineral.isDefined && false) { // TODO: Need to filter accelerant minerals before relying on them; they need to pull the worker across their face relative to the town hall or at least be a straight horizontal shot
            if (onTargetMineral) {
              if (projectedFrames > accelerantFrame) {
                doGatherFromAccelerant()
              }
            } else if (onAccelerantMineral) {
              if (distance < 32 || unit.pixelDistanceEdge(accelerantMineral.get) < 32) {
                doGather()
              }
            } else if (projectedFrames > accelerantFrame) {
              doGatherFromAccelerant()
            }
          } else if ( ! onTargetMineral) {
            doGather()
          }
        }
      } else {
        move(unit, resource.pixel)
      }
    }
    sleep(unit, 1)
  }

  def build(unit: FriendlyUnitInfo, unitClass: UnitClass): Unit = {
    if (unit.unready)       return
    if (autoUnburrow(unit)) return
    // Don't auto-unload! We have a separate process for building Scarabs in loaded Reavers
    unit.bwapiUnit.build(unitClass.bwapiType)
    sleepBuild(unit)
  }

  def build(unit: FriendlyUnitInfo, unitClass: UnitClass, tile: Tile): Unit = {
    unit.agent.setRideGoal(tile.center)
    if (unit.unready)       return
    if (autoUnburrow(unit)) return
    if (autoUnload(unit))   return
    if (unit.pixelDistanceSquared(tile.center) > Math.pow(32.0 * 5.0, 2)) {
      move(unit, tile.center)
      return
    }
    unit.bwapiUnit.build(unitClass.bwapiType, tile.bwapi)
    sleepBuild(unit)
  }

  def tech(unit: FriendlyUnitInfo, tech: Tech): Unit = {
    if (unit.unready) return
    unit.bwapiUnit.research(tech.bwapiTech)
    sleep(unit)
  }

  def upgrade(unit: FriendlyUnitInfo, upgrade: Upgrade): Unit = {
    if (unit.unready) return
    unit.bwapiUnit.upgrade(upgrade.bwapiType)
    sleep(unit)
  }

  def cancel(unit: FriendlyUnitInfo): Unit = {
    if (unit.unready) return
    if (unit.teching) {
      unit.bwapiUnit.cancelResearch()
    } else if (unit.upgrading) {
      unit.bwapiUnit.cancelUpgrade()
    } else if (unit.training) {
      unit.bwapiUnit.cancelTrain()
    } else {
      unit.bwapiUnit.cancelConstruction()
    }
    sleep(unit)
  }

  def rally(unit: FriendlyUnitInfo, pixel: Pixel): Unit = {
    if (unit.unready) return
    unit.bwapiUnit.setRallyPoint(pixel.bwapi)
    unit.lastSetRally = With.frame
    sleep(unit)
  }

  def requestUnload(passenger: FriendlyUnitInfo): Unit = {
    passenger.agent.wantsUnload = true
  }

  def unload(transport: FriendlyUnitInfo, passenger: UnitInfo): Unit = {
    // No sleeping required
    transport.bwapiUnit.unload(passenger.bwapiUnit)
  }

  def addon(unit: FriendlyUnitInfo, unitClass: UnitClass): Unit = {
    if (unit.unready) return
    unit.bwapiUnit.buildAddon(unitClass.bwapiType)
    sleep(unit)
  }

  def buildScarab(unit: FriendlyUnitInfo): Unit = {
    if (unit.unready) return
    unit.bwapiUnit.build(Protoss.Scarab.bwapiType)
    sleep(unit)
  }

  def buildInterceptor(unit: FriendlyUnitInfo): Unit = {
    if (unit.unready) return
    unit.bwapiUnit.build(Protoss.Interceptor.bwapiType)
    sleep(unit)
  }

  def cloak(unit: FriendlyUnitInfo, tech: Tech): Unit = {
    leadFollower(unit, cloak(_, tech))
    if (unit.unready) return
    unit.agent.lastCloak = With.frame
    unit.bwapiUnit.cloak()
    sleep(unit)
  }

  def decloak(unit: FriendlyUnitInfo, tech: Tech): Unit = {
    leadFollower(unit, decloak(_, tech))
    if (unit.unready) return
    unit.bwapiUnit.decloak()
    sleep(unit)
  }

  def burrow(unit: FriendlyUnitInfo): Unit = {
    if (unit.unready) return
    unit.bwapiUnit.burrow()
    sleep(unit)
  }

  def unburrow(unit: FriendlyUnitInfo): Unit = {
    if (unit.unready) return
    unit.bwapiUnit.unburrow()
    sleep(unit)
  }

  def lift(unit: FriendlyUnitInfo): Unit = {
    if (unit.unready) return
    unit.bwapiUnit.lift()
    sleep(unit)
  }

  def defaultEscalation(unit: FriendlyUnitInfo): Unit = {
    lazy val buildDistance = unit.pixelDistanceCenter(unit.intent.toBuildActive.map(b => b.unitClass.tileArea.add(b.tile).center).get)
    if (unit.flying) return
    if (unit.intent.toScoutTiles.nonEmpty)                      unit.agent.escalatePriority(TrafficPriorities.Pardon)
    if (unit.intent.toFinish.isDefined)                         unit.agent.escalatePriority(TrafficPriorities.Bump)
    if (unit.intent.toRepair.isDefined)                         unit.agent.escalatePriority(TrafficPriorities.Bump)
    if (unit.intent.toBuild.nonEmpty)                           unit.agent.escalatePriority(?(buildDistance < 128, TrafficPriorities.Shove, TrafficPriorities.Bump))
    if (unit.agent.toAttack.exists( ! unit.inRangeToAttack(_))) unit.agent.escalatePriority(TrafficPriorities.Nudge)
    if (unit.battle.isDefined && ! unit.agent.shouldFight) {    unit.agent.escalatePriority(TrafficPriorities.Pardon)
      if (unit.matchups.pixelsEntangled > -80)                  unit.agent.escalatePriority(TrafficPriorities.Nudge)
      if (unit.matchups.pixelsEntangled > -48)                  unit.agent.escalatePriority(TrafficPriorities.Bump)
      if (unit.matchups.pixelsEntangled > -16)                  unit.agent.escalatePriority(TrafficPriorities.Shove)
    }
  }

  private def push(unit: FriendlyUnitInfo,  minimumPriority: TrafficPriority, makePush: => Push): Unit = {
    if (unit.flying) return
    unit.agent.escalatePriority(minimumPriority)
    defaultEscalation(unit)
    if (unit.agent.priority > TrafficPriorities.None) {
      With.coordinator.pushes.put(makePush)
    }
  }

  def pushAway(unit: FriendlyUnitInfo, to: Pixel, minimumPriority: TrafficPriority, radius: Double = 48): Unit = {
    push(unit, minimumPriority, new CircularPush(unit.agent.priority, to, radius, unit))
  }

  def pushThrough(unit: FriendlyUnitInfo, to: Pixel, minimumPriority: TrafficPriority, maxDistance: Double = 96): Unit = {
    push(unit, minimumPriority, new UnitLinearGroundPush(unit.agent.priority, unit, ?(unit.pixelDistanceCenter(to) > maxDistance, unit.pixel.project(to, maxDistance), to)))
  }

  private def autoUnburrow(unit: FriendlyUnitInfo): Boolean = {
    if (unit.unready) return false
    if ( ! unit.burrowed) return false
    unburrow(unit)
    true
  }

  private def autoUnload(unit: FriendlyUnitInfo): Boolean = {
    if (unit.unready) return false
    if (unit.transport.isEmpty) return false
    requestUnload(unit)
    true
  }

  private def autoUnburrowUnlessLurkerInRangeOf(unit: FriendlyUnitInfo, to: Pixel): Unit = {
    if ( ! Zerg.Lurker(unit) || unit.pixelDistanceCenter(to) > unit.pixelRangeGround) autoUnburrow(unit)
  }
  
  private def sleepAttack(unit: FriendlyUnitInfo): Unit = {
    sleep(unit, AttackDelay.framesToWaitAfterIssuingAttackOrder(unit))
  }
  
  private def sleepBuild(unit: FriendlyUnitInfo): Unit = {
    // Adapted from https://github.com/tscmoo/tsc-bwai/blame/master/src/unit_controls.h#L1497
    // In practice (1 + 2 * With.latency.latencyFrames) was too low
    sleep(unit, 3 + 2 * With.latency.latencyFrames)
  }
  
  private def sleepReturnCargo(unit:FriendlyUnitInfo): Unit = {
    // Based on https://github.com/tscmoo/tsc-bwai/blame/master/src/unit_controls.h#L1442
    sleep(unit, 8)
  }
  
  def sleep(unit: FriendlyUnitInfo, requiredDelay: Int = 2): Unit = {
    unit.sleepUntil = Maff.vmax(
      With.frame + requiredDelay,
      With.frame + With.latency.turnSize,
      unit.sleepUntil)

    if (With.configuration.trackUnit && (unit.selected || unit.transport.exists(_.selected))) {
      With.configuration.trackUnit = false // This is where you want the trackUnit breakpoint
    }
  }

  private def retrack(): Unit = {
    With.configuration.trackUnit = true
  }

  private def leadFollower(unit: FriendlyUnitInfo, todo: (FriendlyUnitInfo) => Unit): Unit = {
    if (unit.ready) {
      unit.agent.leadFollower = todo
    }
  }
}
