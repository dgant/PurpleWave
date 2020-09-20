package Micro.Actions.Basic

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.SpecificPoints
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.{Disengage, Engage}
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Commands.Move
import Planning.UnitMatchers.UnitMatchWorkers
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Strategery.Benzene
import Utilities.ByOption

object Gather extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.toGather.isDefined
  }
  
  private val combatWindow = GameTime(0, 2)()
  
  override def perform(unit: FriendlyUnitInfo) {

    var resource = unit.agent.toGather.get

    // Move between adjacent bases if threatened
    val baseOriginal = resource.base
    lazy val baseOpposite = unit.base.flatMap(b => b.isNaturalOf.orElse(b.natural))
    if (baseOpposite.nonEmpty && baseOriginal.exists(_.units.exists(threat =>
      ! threat.unitClass.isWorker
      && threat.isEnemy
      && threat.canAttack(unit)
      && threat.pixelsToGetInRange(unit, resource.pixelCenter) < 128))) {
      resource = ByOption.minBy(baseOpposite.get.minerals.filter(_.alive))(_.pixelDistanceEdge(unit)).getOrElse(resource)
    }

    // Gatherer combat micro
    if (unit.battle.nonEmpty) {
      // Burrow from threats
      if (unit.canBurrow
        && unit.matchups.enemies.exists(enemy =>
          ! enemy.isAny(UnitMatchWorkers, Terran.Wraith, Protoss.Arbiter, Protoss.Scout)
          && enemy.pixelDistanceEdge(unit) < enemy.pixelRangeAgainst(unit) + 32)
          && ! With.grids.enemyDetection.isDetected(unit.tileIncludingCenter)) {
        With.commander.burrow(unit)
      }

      // Stupid siege tank defense
      if (unit.matchups.threatsInRange.exists(t => t.is(Terran.SiegeTankSieged) && t.pixelDistanceEdge(resource) < 32 * 13 && t.base.exists(_.owner.isUs))) {
        Engage.consider(unit)
      }

      // Help with fights when appropriate
      lazy val beckonedToFight  = unit.matchups.targets.exists(target =>
        ! target.unitClass.isWorker
        && (target.canAttack || target.unitClass.rawCanAttack)
        && target.pixelDistanceCenter(unit)     < With.configuration.workerDefenseRadiusPixels
        && target.pixelDistanceCenter(resource) < With.configuration.workerDefenseRadiusPixels)
      if (unit.totalHealth > 32 && beckonedToFight) {
        Engage.consider(unit)
      }

      val lethalStabbers = unit.matchups.threatsInRange.filter(threat =>
        threat.unitClass.melee
        && (if (threat.presumptiveTarget.contains(unit)) 2 else 1) * threat.damageOnNextHitAgainst(unit) >= unit.totalHealth)

      if (lethalStabbers.nonEmpty && unit.base.isDefined) {
        val goals = (unit.base.get.resources ++ unit.base.flatMap(_.townHall))
          .filter(goal =>
            ((unit.carryingGas || unit.carryingMinerals)  && goal.unitClass.isTownHall  && goal.isOurs) ||
            (!unit.carryingGas                            && goal.unitClass.isGas       && goal.isOurs) ||
            (!unit.carryingMinerals                       && goal.unitClass.isMinerals))

        val bestGoal = ByOption.maxBy(goals)(resource => lethalStabbers.map(stabber => resource.pixelDistanceCenter(stabber)).max)
        if (bestGoal.exists(_.unitClass.isTownHall)) {
          With.commander.returnCargo(unit)
        } else if (bestGoal.isDefined) {
          if (unit.pixelDistanceEdge(bestGoal.get) < 32) {
            Potshot.consider(unit)
          } else {
            With.commander.gather(unit, bestGoal.get)
          }
        }
      }

      Potshot.consider(unit)

      // Run away if threatened during transfer
      lazy val zoneNow          = unit.zone
      lazy val zoneTo           = resource.zone
      lazy val mainAndNatural   = Vector(With.geography.ourMain, With.geography.ourNatural).map(_.zone)
      lazy val transferring     = ! unit.base.exists(_.owner.isUs) && zoneNow != zoneTo && ! (mainAndNatural.contains(zoneNow) && mainAndNatural.contains(zoneTo))
      lazy val threatened       = unit.battle.isDefined && unit.matchups.framesOfSafety < combatWindow && unit.matchups.threats.exists(!_.unitClass.isWorker)
      lazy val threatCloser     = unit.matchups.threats.exists(_.pixelDistanceCenter(resource.pixelCenter) < unit.pixelDistanceCenter(resource.pixelCenter))
      if (transferring
        && threatened
        && threatCloser
        && (unit.visibleToOpponents || unit.matchups.framesOfSafety < unit.unitClass.framesToTurn180)) {
        unit.agent.canFight = false
        Disengage.consider(unit)
      }
    }

    // Benzene travel hack
    if (resource.zone != unit.zone
      && unit.zone == With.geography.ourMain.zone
      && Benzene.matches) {
      unit.agent.toTravel = Some(SpecificPoints.middle)
      Move.delegate(unit)
    }
    
    With.commander.gather(unit, resource)
  }
}
