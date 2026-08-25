package Micro.Actions.Combat.Tactics

import Debugging.Visualizations.Forces
import Information.Geography.Pathfinding.PathfindProfile
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Physics.Force
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Scouting.Scout
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Heuristics.Potential
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?
import Utilities.UnitFilters.{IsBuilding, IsWarrior, IsWorker}

object Tickle extends Action {

  override def allowed(tickler: FriendlyUnitInfo): Boolean = IsWorker(tickler)

  override protected def perform(tickler: FriendlyUnitInfo): Unit = {

    // Return resources now so we can mine again later
    if (tickler.carrying && tickler.base.exists(_.owner.isUs)) {
      Commander.returnCargo(tickler)
      return
    }

    tickler.agent.toAttack =
      Maff.minBy(
        Maff.orElse(
          tickler.matchups.threats.filter(Terran.Vulture),
          tickler.matchups.threats.filter(Terran.Marine),
          tickler.matchups.threats.filter(Protoss.Dragoon),
          tickler.matchups.threats.filter(Protoss.PhotonCannon),
          tickler.matchups.threats.filter(Protoss.Zealot).filter(x => tickler.totalHealth > ?(x.presumptiveTarget.contains(tickler), 32, 16)),
          tickler.matchups.threats.filter(Zerg.Zergling).filter(x => tickler.totalHealth > 10)))(_.totalHealth)
    Commander.attack(tickler)

    val strengthGlobalUs    = With.units.ours   .filter(IsWorker).map(strengthGlobal).sum
    val strengthGlobalFoe   = With.units.enemy  .filter(IsWorker).map(strengthGlobal).sum
    val strengthGlobalRatio = strengthGlobalUs / Math.max(1.0, strengthGlobalFoe)
    val strength            = tickler.matchups.allUnits.filter(_.unitClass.attacksGround).map(u => strengthLocal(tickler, u) * Maff.or1(strengthGlobalRatio, u.isFriendly)).sum

    if (strength > 0 || tickler.matchups.pixelsToThreatRange.forall(_ > 256)) {
      //repair(tickler)
    }
    if (strength > 0 && tickler.totalHealth > 10) {
      fight(tickler)
    } else if (tickler.tile.enemyRangeGround > 0 && (tickler.matchups.threatsInRange.nonEmpty || tickler.matchups.threats.exists(_.orderTarget.contains(tickler)))) {
      flee(tickler)
    }

    arson(tickler)
    Commander.move(tickler)
  }

  private def strengthGlobal(unit: UnitInfo): Double = {
    unit.totalHealth
  }

  private def strengthLocal(tickler: FriendlyUnitInfo, u: UnitInfo): Double = {
    val sign        = ?(u.isFriendly, 1, ?(u.isEnemy, -1, 0))
    val proximity   = Math.max(0, 1 - Math.max(0, u.pixelsToGetInRange(tickler) - tickler.matchups.pixelsToTargetRange.getOrElse(0d)) / 320.0)
    val health      = u.totalHealth / u.unitClass.maxTotalHealth.toDouble
    val fighting    = u.matchups.allies.filter(IsWarrior).exists(_.matchups.threatsInRange.nonEmpty)
    val lowAllyHP   = Maff.min(tickler.matchups.allies.filter(Protoss.Zealot).filter(_.matchups.threatsInPixels(8).nonEmpty).map(_.totalHealth.toDouble)).getOrElse(1000.0)
    val assisting   = Maff.clamp(tickler.totalHealth / Math.max(1.0, lowAllyHP), 1.0, 2.0)
    val hysteresis  = ?(fighting, 1.1, 0.9)
    val busyness    = ?(u.gathering || u.constructing, 1.0, 1.0)
    val strength    = ?(Terran.SCV(tickler), 1.35, 1.0)
    val output      = sign * proximity * health * hysteresis * busyness * assisting * strength
    output
  }

  def fight(tickler: FriendlyUnitInfo): Unit = {
    val targets = With.units.enemy.filter(t =>
      t.unitClass.attacksGround
        && t.visible
        && (t.zone == tickler.zone || tickler.pixelsToGetInRange(t) < 320)
        && ( ! IsWorker(t) || t.matchups.targetsInRange.nonEmpty || t.constructing || tickler.pixelsToGetInRange(t) < 16) || t.orderTarget.exists(_.isOurs))
    val target = Maff.minBy(targets)(t => t.totalHealth + 1.0 * tickler.pixelsToGetInRange(t))
    tickler.agent.toAttack = target
    if (target.isDefined
      && ! Terran.SCV(tickler)
      && target.exists(Terran.SCV)
      && ! tickler.readyForAttackOrder
      && tickler.framesToConnectDamage(target.get) > tickler.framesToBeReadyForAttackOrder) {
      flee(tickler)
    }
    Commander.attack(tickler)
  }

  def arson(tickler: FriendlyUnitInfo): Unit = {
    val targets = With.units.enemy
      .filter(IsBuilding)
      .map(b => (b, Maff.minBy(b.tileArea.tilesSurrounding.filter(t => t.walkable && t.enemyRangeGround <= 0))(tickler.pixelDistanceSquared)))
      .filter(_._2.isDefined)
      .map(b => (b._1, b._2.get.center))
      .toVector
    if (targets.isEmpty) {
      Scout(tickler)
    } else {
      val target = targets.minBy(t =>
        t._1.totalHealth
          - 0.5 * Maff.or0(t._1.unitClass.maxTotalHealth, t._1.complete)
          - Maff.or0(1000, t._1.isAny(Terran.Barracks, Terran.Factory, Protoss.Gateway, Protoss.PhotonCannon, Zerg.SunkenColony, Zerg.CreepColony, Zerg.SpawningPool)))
      val pixel     = target._2
      val distance  = tickler.pixelDistanceTravelling(pixel)
      if (distance > 80) {
        tickler.agent.decision.set(pixel)
        val pathLengthMinimum             = 7
        val pathfindProfile               = new PathfindProfile(tickler.tile)
        pathfindProfile.end               = Some(pixel.tile)
        pathfindProfile.lengthMaximum     = Some(30)
        pathfindProfile.threatMaximum     = Some(0)
        pathfindProfile.employGroundDist  = true
        pathfindProfile.costOccupancy     = 1
        pathfindProfile.costRepulsion     = 1
        pathfindProfile.costThreat        = 6
        pathfindProfile.repulsors         = MicroPathing.getPathfindingRepulsors(tickler)
        pathfindProfile.unit              = Some(tickler)
        val path                          = pathfindProfile.find
        MicroPathing.tryMovingAlongTilePath(tickler, path)
        Commander.move(tickler)
      } else {
        tickler.agent.toAttack = Some(target._1)
        Commander.attack(tickler)
      }
    }
  }

  def flee(tickler: FriendlyUnitInfo): Unit = {
    tickler.agent.home.set(tickler.agent.destinationFinal.pixel)
    Retreat(tickler)
    val mineral = With.geography.ourMain.minerals.headOption.getOrElse(With.units.ours.filter(IsBuilding).head)
    val away    = Potential.softAvoidThreatRange(tickler)
    val out     = MicroPathing.getGroundWaypointToPixel(tickler.pixel, mineral.pixel)
    val awayN   = away.normalize
    val outN    = new Force(out.subtract(tickler.pixel)).normalize
    if (awayN.dot(outN) > 0.7) {
      Commander.rightClick(tickler, mineral)
    } else {
      tickler.agent.forces.put(Forces.threat, away)
      tickler.agent.forces.put(Forces.travel, Potential.towards(tickler, tickler.agent.destinationFinal.pixel.get.zone.centroid.center))
      tickler.agent.forces.put(Forces.spacing, Potential.preferSpacing(tickler))
      MicroPathing.moveForcefully(tickler)
    }
  }

  def repair(tickler: FriendlyUnitInfo): Unit = {
    if (Terran.SCV(tickler)) {
      val medics = tickler.matchups.allies.filter(_.orderTarget.contains(tickler))
      if (medics.nonEmpty) {
        val repairables = tickler.matchups.allies.filter(a => Terran.SCV(a) && a.totalHealth < 60 && tickler.inRangeToAttack(a))
        val mostInjured = Maff.minBy(repairables)(_.totalHealth)
        val attacker    = tickler.matchups.allies.exists(_.friendly.exists(_.agent.toAttack.isDefined))
        if (attacker && mostInjured.exists(_.totalHealth < 35)) {
          Commander.rightClick(tickler, mostInjured.get)
        } else if (tickler.matchups.targetsInRange.nonEmpty) {
          tickler.agent.toAttack = Maff.minBy(tickler.matchups.targetsInRange)(_.totalHealth)
          Commander.attack(tickler)
        } else {
          val medic = medics.minBy(_.totalHealth)
          if (medic.totalHealth < 60) {
            Commander.rightClick(tickler, medics.minBy(_.totalHealth))
          } else {
            Commander.stop(tickler)
          }
        }
      } else if (With.self.minerals > 10) {
        val repairables = tickler.matchups.allies.filter(a => Terran.SCV(a) && a.totalHealth < 32 && (tickler.pixelsToGetInRange(a) < 12 || tickler.matchups.pixelsToThreatRange.forall(p => a.matchups.pixelsToThreatRange.forall(_ < p))) && a.totalHealth <= tickler.totalHealth)
        val repairable = Maff.minBy(repairables)(_.pixelDistanceEdge(tickler))
        repairable.foreach(Commander.rightClick(tickler, _))
      }
    }
  }
}
