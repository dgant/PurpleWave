package Micro.Actions.Combat.Tactics

import Information.Geography.Pathfinding.PathfindProfile
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Planning.UnitMatchers.{MatchBuilding, MatchWorkers}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Strategery.Strategies.AllRaces.{WorkersKill, WorkersRaze, WorkersSpread, WorkersUnite}

object Tickle extends Action {
  
  override def allowed(tickler: FriendlyUnitInfo): Boolean = {
    tickler.agent.intent.canTickle
  }

  def workersSpread : Boolean = WorkersSpread.registerActive
  def workersUnite  : Boolean = WorkersUnite.registerActive
  def workersRaze   : Boolean = WorkersRaze.registerActive
  def workersKill   : Boolean = WorkersKill.registerActive

  override protected def perform(tickler: FriendlyUnitInfo) {

    // Return resources now so we can mine again later
    if (tickler.carrying && tickler.base.exists(_.owner.isUs)) {
      Commander.returnCargo(tickler)
      return
    }

    // Potential improvements:
    // * Use convex hull to ensure we don't get trapped in the worker line
    // * Fight when injured if everyone nearby is targeting someone else
    // * Don't get pulled out of the base

    if (tickler.unready) return

    var attack                = true

    val zone                  = tickler.agent.toTravel.get.zone
    val exit                  = zone.edges.map(_.pixelCenter).sortBy(_.groundPixels(With.geography.home)).headOption.getOrElse(With.geography.home.pixelCenter)
    val hurtThreshold         = 30
    val dyingThreshold        = 6
    val hurt                  = tickler.totalHealth < hurtThreshold
    val dying                 = tickler.totalHealth < dyingThreshold
    val enemies               = tickler.matchups.threats
    val enemyFighters         = tickler.matchups.threats.filter(_.isBeingViolent)
    val enemiesAttackingUs    = enemies.filter(_.isBeingViolentTo(tickler))
    val allyFighters          = tickler.matchups.allies
    val strength              = (units: Iterable[UnitInfo]) => units.size * units.map(_.totalHealth).sum
    val ourStrength           = strength(allyFighters :+ tickler)
    val enemyStrength         = strength(enemies)
    val enemyFighterStrength  = strength(enemyFighters)

    // We want to avoid big drilling stacks
    val centroidSoft  = PurpleMath.centroid(enemies.map(_.pixel))
    val centroidHard  = if (enemies.size > 3) PurpleMath.centroid(enemies.sortBy(_.pixelDistanceCenter(centroidSoft)).take(enemies.size/2).map(_.pixel)) else centroidSoft //Don't centroid an empty list

    // Get in their base!
    if ( ! tickler.zone.bases.exists(_.owner.isEnemy)
      && ! enemies.exists(_.pixelDistanceEdge(tickler) < 48.0)
      && enemiesAttackingUs.size < 1) {
      Commander.move(tickler)
      return
    }

    // Don't get surrounded if we're trying to unite
    if (
      workersUnite
      && zone.bases.exists(_.harvestingArea.contains(tickler.tile)
      && enemies.exists(enemy =>
        enemy.zone == zone
        && enemy.pixelDistanceEdge(tickler) < 64.0
        && enemy.pixelDistanceCenter(exit) < tickler.pixelDistanceCenter(exit)))) {
      runAway(tickler)
      return
    }

    // Try to avoid dying. Let our regeneration work for us.
    if (dying && ! tickler.is(Terran.SCV)) {
      attack = false
    }

    // Don't take losing fights
    if (enemiesAttackingUs.map(_.totalHealth).sum > tickler.totalHealth) {
      attack = false
    }

    // If violent enemies completely overpower us, let's back off
    if (ourStrength < enemyFighterStrength && hurt) {
      attack = false
    }

    // Wait for re-enforcements
    val workersTotal  = With.units.countOurs(MatchWorkers)
    val workersHere   = With.units.countOursP(u => u.unitClass.isWorker && u.zone == zone)
    if (workersHere * 2 < workersTotal  // Tomorrow, there'll be more of us.
      && enemies.size > 4) {            // Verus 4-Pool need to start dealing damage immediately
      attack = false
    }

    // Stay close to the fight
    if (tickler.matchups.threats.forall(_.pixelDistanceEdge(tickler) > 64)) {
      attack = true
    }

    // Heal up before going back in
    if (dying && tickler.matchups.threats.exists(_.pixelDistanceEdge(tickler) < 128)) {
      attack = false
    }

    // If we completely overpower the enemy, let's go kill 'em.
    val weOverpower = ourStrength > enemyStrength
    if (weOverpower) {
      attack = true
    }

    // Lastly, if they've started training combat units, we are ALL IN
    if ( ! enemies.forall(MatchWorkers)) {
      attack = true
    }

    if (attack) {
      // No static defense allowed!
      val staticDefense = tickler.matchups.targets.filter(u => u.unitClass.isBuilding && u.unitClass.rawCanAttack)
      if (staticDefense.nonEmpty) {
        tickler.agent.toAttack = staticDefense
          .sortBy(_.remainingCompletionFrames)
          .sortBy(_.totalHealth)
          .headOption
        Commander.attack(tickler)
      }

      val targets = tickler.matchups.targets.filter(target =>
        // Ignore units outside their bases
        (
          target.zone == zone
            // ...unless they're fighting us! Or building a building.
            || target.constructing
            || tickler.inRangeToAttack(target)
            || (
            target.isBeingViolent &&
              target.pixelDistanceTravelling(zone.centroid) <= tickler.pixelDistanceTravelling(zone.centroid)
            )
          )
          // Kill combat units first unless we're trying to raze buildings
          && (target.canAttack || workersRaze)
          // Don't get distracted by workers leaving the base (eg. scouts)
          && (
          target.targetPixel.forall(_.zone == zone)
            || tickler.inRangeToAttack(target)
            || target.isBeingViolent
            || tickler.constructing
          ))
      if (targets.isEmpty) {
        destroyBuildings(tickler)
        return
      }

      // Let's pick the outermost target while avoiding drilling stacks
      val nearestTargetDistance = targets.map(_.pixelDistanceCenter(exit)).min
      val validTargets = targets.filter(_.pixelDistanceCenter(exit) - 16.0 <= nearestTargetDistance)
      val razing = workersRaze
      val bestTarget =
        validTargets
          // Prefer vulnerable workers
          .sortBy(target => target.totalHealth * target.pixelDistanceEdge(tickler))
          // If razing: Prefer the weakest building
          .sortBy(target => if (razing && target.unitClass.isBuilding) razeCost(tickler, target) else 0)
          // But definitely prefer enemy combat units in range to attack us
          .sortBy(target => !(target.canAttack(tickler) && target.inRangeToAttack(tickler)))
          .headOption
          .getOrElse(targets.minBy(_.pixelDistanceCenter(exit)))

      if (tickler.readyForAttackOrder
        || (bestTarget.canAttack(tickler) && bestTarget.pixelRangeAgainst(tickler) > tickler.pixelRangeAgainst(bestTarget))
        || tickler.matchups.threats.forall(t => t.constructing || t.framesToGetInRange(tickler) > 48)
        || tickler.cooldownLeft - With.latency.framesRemaining <= targets.map(target => tickler.framesToTravelPixels(tickler.pixelDistanceEdge(target))).min) {
        tickler.agent.toAttack = Some(bestTarget)
        Commander.attack(tickler)
        return
      }

      // SCVs shouldn't kite
      if (tickler.is(Terran.SCV)) {
        return
      }
    }

    // We're not attacking, so let's hang out and wait for opportunities
    if (enemiesAttackingUs.nonEmpty || enemies.exists(e => e.canAttack(tickler) && ! e.constructing && ! e.gathering && e.pixelDistanceEdge(tickler) < 64.0)) {
      if ( ! weOverpower) {
        runAway(tickler)
      }
    }
    else {
      val freebies = enemies.find(freebie =>
        (freebie.unitClass.isBuilding || freebie.constructing)
          && freebie.armorHealth < 5
          && enemies.forall(defender =>
          defender != freebie
            && tickler.pixelDistanceEdge(defender) >
            tickler.pixelDistanceEdge(freebie)))
      tickler.agent.toAttack = freebies
      Commander.attack(tickler)
      Retreat.consider(tickler)
    }
  }

  private def razeCost(tickler: FriendlyUnitInfo, target: UnitInfo): Double = {
    val importance =
      if (target.isAny(Protoss.PhotonCannon, Zerg.SpawningPool))
        500.0
      else if (target.isAny(Terran.Barracks, Protoss.Gateway, Zerg.CreepColony))
        50.0
      else if (target.isAny(Terran.Factory, Terran.Bunker, Protoss.ShieldBattery, Protoss.Pylon))
        5.0
      else
        1.0
    val health = if (target.complete) target.totalHealth else target.unitClass.maxTotalHealth
    health / importance
  }

  private def runAway(tickler: FriendlyUnitInfo) {

    if (workersSpread) {
      // Pathfind away
      val pathLengthMinimum = 7
      val maximumDistance = 10
      val profile = new PathfindProfile(tickler.tile)
      profile.end = None
      profile.lengthMinimum = Some(pathLengthMinimum)
      profile.lengthMaximum = Some(maximumDistance)
      profile.threatMaximum = Some(0)
      profile.employGroundDist = true
      profile.costOccupancy = 1f
      profile.costRepulsion = 8f
      profile.repulsors = MicroPathing.getPathfindingRepulsors(tickler)
      profile.unit = Some(tickler)
      val path = profile.find
      MicroPathing.tryMovingAlongTilePath(tickler, path)
    }

    // Mineral walk
    tickler.agent.toGather = With.geography.ourBases.flatMap(_.minerals).headOption
    tickler.agent.toTravel = Some(tickler.agent.origin)
    Commander.gather(tickler)
    Commander.move(tickler)
  }
  
  private def destroyBuildings(unit: FriendlyUnitInfo) {
    // Vs. 4-pool they will often be left with just an egg or two.
    // We need to surround it and do as much damage to it as possible
    val egg = With.units.enemy.view.filter(Zerg.Egg).toVector.sortBy(_.totalHealth).headOption
    lazy val nonEgg = With.units.enemy.view.filter(MatchBuilding).toVector.sortBy(_.totalHealth).headOption
    unit.agent.toAttack = egg.orElse(nonEgg)
    Commander.attack(unit)
  }
}
