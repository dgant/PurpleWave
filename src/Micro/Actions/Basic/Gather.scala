package Micro.Actions.Basic

import Information.Intelligenze.Fingerprinting.Generic.GameTime
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

    if (unit.battle.nonEmpty) {
      lazy val resource = unit.agent.toGather.get
      lazy val zoneNow = unit.zone
      lazy val zoneTo = resource.zone
      lazy val mainAndNat = Vector(With.geography.ourMain, With.geography.ourNatural).map(_.zone)
      lazy val transferring = !unit.base.exists(_.owner.isUs) && zoneNow != zoneTo && !(mainAndNat.contains(zoneNow) && mainAndNat.contains(zoneTo))
      lazy val threatened = unit.battle.isDefined && unit.matchups.framesOfSafety < combatWindow && unit.matchups.threats.exists(!_.unitClass.isWorker)
      lazy val threatCloser = unit.matchups.threats.exists(_.pixelDistanceCenter(resource.pixelCenter) < unit.pixelDistanceCenter(resource.pixelCenter))
      lazy val atResource = unit.pixelDistanceCenter(resource) < With.configuration.workerDefenseRadiusPixels
      lazy val beckoned = unit.battle.isDefined && unit.matchups.targets.exists(target =>
        !target.unitClass.isWorker
          && target.pixelDistanceCenter(unit) < With.configuration.workerDefenseRadiusPixels
          && target.base.exists(_.units.exists(resource => resource.resourcesLeft > 0 && target.pixelDistanceCenter(resource) < With.configuration.workerDefenseRadiusPixels)))

      if (unit.canBurrow
        && unit.matchups.enemies.exists(enemy =>
        ! enemy.isAny(UnitMatchWorkers, Terran.Wraith, Protoss.Arbiter, Protoss.Scout)
        && enemy.pixelDistanceEdge(unit) < enemy.pixelRangeAgainst(unit) + 32)
        && ! With.grids.enemyDetection.isDetected(unit.tileIncludingCenter)) {
        With.commander.burrow(unit)
      }

      // Stupid siege tank defense
      if (unit.matchups.threatsInRange.exists(t => t.is(Terran.SiegeTankSieged) && t.pixelDistanceEdge(resource) < 32 * 13)) {
        Engage.consider(unit)
      }

      if (atResource && unit.totalHealth > 32 && beckoned) {
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
          unit.agent.toGather = bestGoal
          if (unit.pixelDistanceEdge(bestGoal.get) < 32) {
            Potshot.consider(unit)
          }
          With.commander.gather(unit, bestGoal.get)
        }
      }

      Potshot.consider(unit)

      if (transferring
        && threatened
        && threatCloser
        && (unit.visibleToOpponents || unit.matchups.framesOfSafety < unit.unitClass.framesToTurn180)) {
        unit.agent.canFight = false
        Disengage.consider(unit)
      }
    }

    // Total hack
    if (unit.agent.toGather.exists(_.zone != unit.zone)
      && unit.base.contains(With.geography.ourMain)
      && Benzene.matches) {
      unit.agent.toTravel = Some(SpecificPoints.middle)
      Move.delegate(unit)
    }
    
    With.commander.gather(unit, unit.agent.toGather.get)
  }
}
