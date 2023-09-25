package Micro.Actions.Basic

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Targeting.Target
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Utilities.UnitFilters.IsWorker
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Strategery.Benzene
import Utilities.?
import Utilities.Time.{Minutes, Seconds}

object Gather extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.agent.toGather.isDefined
  
  private val combatWindow = Seconds(2)()

  val defenseRadiusPixels = 160

  private def resourceThreatened(resource: UnitInfo): Boolean = resource.tile.enemyRangeGround > 0 || resource.tile.add(1, 0).enemyRangeGround > 0

  private def minerThreatenedAt(miner: FriendlyUnitInfo, resource: UnitInfo): Boolean = (
    resourceThreatened(resource)
    && resource.base.exists(_.enemies.exists(e =>
      e.canAttackGround
      && e.isNone(IsWorker, Terran.Wraith, Protoss.Arbiter, Protoss.Scout, Zerg.Mutalisk)
      && e.pixelsToGetInRange(miner, resource.pixel) < defenseRadiusPixels)))
  
  override def perform(unit: FriendlyUnitInfo): Unit = {

    def resource = unit.agent.toGather.get

    // Gatherer combat micro
    if (unit.battle.nonEmpty) {

      // Move between resources if ours isn't safe to mine and we hope help will arrive
      val baseOriginal      = resource.base
      lazy val baseOpposite = baseOriginal.flatMap(b => b.naturalOf.orElse(b.natural))
      lazy val baseRemote   = Maff.minBy(With.geography.ourBases.filterNot(baseOriginal.contains))(_.heart.groundPixels(baseOriginal.map(_.heart).getOrElse(resource.tileTopLeft)))
      lazy val basePaired   = baseOpposite.orElse(baseRemote).filter(_.isOurs)
      if (minerThreatenedAt(unit, resource)) {
        var       alternativeMineralLocal   = Maff.minBy(baseOriginal .map(_.minerals.view.filterNot(minerThreatenedAt(unit, _))).getOrElse(Seq.empty))(_.pixelDistanceEdge(unit))
        lazy val  alternativeMineralRemote  = Maff.minBy(basePaired   .map(_.minerals.view.filterNot(minerThreatenedAt(unit, _))).getOrElse(Seq.empty))(_.pixelDistanceEdge(unit))
        alternativeMineralLocal = None // TODO: Be smarter about deciding when to abandon base
        unit.agent.toGather = (alternativeMineralLocal.toSeq ++ alternativeMineralRemote).find( ! minerThreatenedAt(unit, _)).orElse(Some(resource))
      }

      // Help initial scout get home
      if (With.frame < Minutes(8)() && unit.metro.exists(_.bases.exists(_.isEnemy)) && unit.matchups.threats.exists( ! _.unitClass.melee) && unit.visibleToOpponents) {
        Commander.gather(unit)
        return
      }

      // Burrow from threats
      if (unit.canBurrow
        && unit.matchups.enemies.exists(enemy =>
          enemy.isNone(IsWorker, Terran.Wraith, Protoss.Arbiter, Protoss.Scout)
          && enemy.pixelDistanceEdge(unit) < enemy.pixelRangeAgainst(unit) + 32)
          && ! unit.tile.enemyDetected) {
        Commander.burrow(unit)
      }

      // Help with fights when appropriate
      lazy val beckonedToFight = (
        unit.battle.isDefined
        && unit.base.exists(base =>
          base.isOurs
          && base.enemies.exists(target =>
            ! target.unitClass.isWorker
            && ! target.unitClass.isFlyer
            && target.unitClass.attacksOrCastsOrDetectsOrTransports
            && target.pixelDistanceCenter(resource) < defenseRadiusPixels
            && unit.base.map(_.heart.center).forall(target.pixelDistanceCenter(_) < defenseRadiusPixels))))

      if (unit.totalHealth > 32 && beckonedToFight) {
        Target.choose(unit)
        Commander.attack(unit)
      }

      // Escape dangerous melee units
      lazy val stabbers        = unit.matchups.threatsInRange.filter(_.unitClass.melee)
      lazy val stabbingLethal  = stabbers.map(e => ?(e.presumptiveTarget.contains(unit), 3, 2) * e.damageOnNextHitAgainst(unit)).sum >= unit.totalHealth
      if (unit.tile.enemyRangeGround > 0 && unit.base.isDefined && stabbingLethal) {
        val drillGoal = (unit.base.get.resources ++ unit.base.flatMap(_.townHall))
          .filter(goal =>
            ((unit.carryingGas || unit.carryingMinerals)  && goal.unitClass.isTownHall  && goal.isOurs) ||
            (!unit.carryingGas                            && goal.unitClass.isGas       && goal.isOurs) ||
            (!unit.carryingMinerals                       && goal.unitClass.isMinerals))
        val bestGoal = Maff.maxBy(drillGoal)(resource => stabbers.map(stabber => resource.pixelDistanceCenter(stabber)).max)
        if (bestGoal.exists(_.unitClass.isTownHall)) {
          Commander.returnCargo(unit)
        } else if (bestGoal.isDefined) {
          // If we expect to die anyway
          if (unit.pixelDistanceEdge(bestGoal.get) < 32) {
            Potshot(unit)
          } else {
            unit.agent.toGather = bestGoal
            Commander.gather(unit)
          }
        }
      }

      if (unit.base.exists(_.isOurs)) {
        Potshot(unit)
      }

      // Run away if threatened during transfer
      lazy val zoneNow        = unit.zone
      lazy val zoneTo         = resource.zone
      lazy val mainAndNatural = Vector(With.geography.ourMain, With.geography.ourNatural).map(_.zone)
      lazy val transferring   = ! unit.base.exists(_.owner.isUs) && zoneNow != zoneTo && ! (mainAndNatural.contains(zoneNow) && mainAndNatural.contains(zoneTo))
      lazy val threatened     = unit.battle.isDefined && unit.matchups.framesOfSafety < combatWindow && ! unit.matchups.threats.forall(IsWorker)
      lazy val threatCloser   = unit.matchups.threats.exists(_.pixelDistanceCenter(resource.pixel) < unit.pixelDistanceCenter(resource.pixel))
      if (transferring
        && threatened
        && threatCloser
        && (unit.visibleToOpponents || ! unit.matchups.withinSafetyMargin || unit.zone.edges.exists(_.contains(unit.pixel)))) {
        Retreat(unit)
      }
    }

    // Take safe/hidden route to expansion
    if (unit.metro != resource.metro && ! unit.carrying && unit.agent.decision.setAsWaypoint(MicroPathing.getWaypointAlongTilePath(unit, MicroPathing.getSneakyPath(unit, Some(resource.tile.walkableTile)))).isDefined) {
      Commander.move(unit)
    }

    // Benzene travel hack
    if (Benzene() && resource.zone != unit.zone && unit.zone == With.geography.ourMain.zone) {
      Commander.move(unit)
    }
    
    Commander.gather(unit)
  }
}
