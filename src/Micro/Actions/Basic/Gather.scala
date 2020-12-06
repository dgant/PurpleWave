package Micro.Actions.Basic

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.DefaultCombat.{Disengage, Engage}
import Micro.Actions.Combat.Tactics.Potshot
import Planning.UnitMatchers.UnitMatchWorkers
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Strategery.Benzene
import Utilities.ByOption

object Gather extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.agent.toGather.isDefined
  
  private val combatWindow = GameTime(0, 2)()

  val defenseRadiusPixels = 160
  
  override def perform(unit: FriendlyUnitInfo) {

    def resource = unit.agent.toGather.get

    // Gatherer combat micro
    if (unit.battle.nonEmpty) {

      // Move between bases if resource isn't safe to mine
      val baseOriginal = resource.base
      lazy val baseOpposite = unit.base.flatMap(b => b.isNaturalOf.orElse(b.natural))
      lazy val baseRemote = ByOption.minBy(With.geography.ourBases.filterNot(baseOriginal.contains))(_.heart.groundPixels(baseOriginal.map(_.heart).getOrElse(resource.tileTopLeft)))
      lazy val basePaired = baseOpposite.orElse(baseRemote)
      if (
        unit.visibleToOpponents
        && basePaired.exists(_.owner.isUs)
        && unit.matchups.threats.exists(threat => ! threat.unitClass.isWorker && threat.pixelsToGetInRange(unit, resource.pixelCenter) < defenseRadiusPixels)) {
        unit.agent.toGather = ByOption.minBy(basePaired.get.minerals.filter(_.alive))(_.pixelDistanceEdge(unit)).orElse(unit.agent.toGather)
      }

      // Burrow from threats
      if (unit.canBurrow
        && unit.matchups.enemies.exists(enemy =>
          ! enemy.isAny(UnitMatchWorkers, Terran.Wraith, Protoss.Arbiter, Protoss.Scout)
          && enemy.pixelDistanceEdge(unit) < enemy.pixelRangeAgainst(unit) + 32)
          && ! With.grids.enemyDetection.isDetected(unit.tileIncludingCenter)) {
        With.commander.burrow(unit)
      }

      // Help with fights when appropriate
      lazy val beckonedToFight  = unit.matchups.targets.exists(target =>
        ! target.unitClass.isWorker
        && (target.canAttack || target.unitClass.rawCanAttack)
        && target.pixelDistanceCenter(unit)     < defenseRadiusPixels
        && target.pixelDistanceCenter(resource) < defenseRadiusPixels)
      if (unit.totalHealth > 32 && beckonedToFight) {
        Engage.consider(unit)
      }

      // Escape dangerous melee units
      val lethalStabbers = unit.matchups.threatsInRange.filter(threat =>
        threat.unitClass.melee
        && (if (threat.presumptiveTarget.contains(unit)) 2 else 1) * threat.damageOnNextHitAgainst(unit) >= unit.totalHealth)
      if (lethalStabbers.nonEmpty && unit.base.isDefined) {
        val drillGoal = (unit.base.get.resources ++ unit.base.flatMap(_.townHall))
          .filter(goal =>
            ((unit.carryingGas || unit.carryingMinerals)  && goal.unitClass.isTownHall  && goal.isOurs) ||
            (!unit.carryingGas                            && goal.unitClass.isGas       && goal.isOurs) ||
            (!unit.carryingMinerals                       && goal.unitClass.isMinerals))
        val bestGoal = ByOption.maxBy(drillGoal)(resource => lethalStabbers.map(stabber => resource.pixelDistanceCenter(stabber)).max)
        if (bestGoal.exists(_.unitClass.isTownHall)) {
          With.commander.returnCargo(unit)
        } else if (bestGoal.isDefined) {
          // If we expect to die anyway
          if (unit.pixelDistanceEdge(bestGoal.get) < 32) {
            Potshot.consider(unit)
          } else {
            unit.agent.toGather = bestGoal
            With.commander.gather(unit)
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
    if (Benzene.matches && resource.zone != unit.zone && unit.zone == With.geography.ourMain.zone) {
      With.commander.move(unit)
    }
    
    With.commander.gather(unit)
  }
}
