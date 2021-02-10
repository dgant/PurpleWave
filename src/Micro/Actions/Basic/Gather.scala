package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Actions.Combat.Targeting.Target
import Micro.Agency.Commander
import Planning.UnitMatchers.MatchWorkers
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Strategery.Benzene
import Utilities.{ByOption, Seconds}

object Gather extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.agent.toGather.isDefined
  
  private val combatWindow = Seconds(2)()

  val defenseRadiusPixels = 160
  
  override def perform(unit: FriendlyUnitInfo) {

    def resource = unit.agent.toGather.get

    // Gatherer combat micro
    if (unit.battle.nonEmpty) {

      // Move between bases if resource isn't safe to mine and we hope help will arrive
      if (With.reaction.sluggishness < 2) {
        val baseOriginal = resource.base
        lazy val baseOpposite = baseOriginal.flatMap(b => b.isNaturalOf.orElse(b.natural))
        lazy val baseRemote = ByOption.minBy(With.geography.ourBases.filterNot(baseOriginal.contains))(_.heart.groundPixels(baseOriginal.map(_.heart).getOrElse(resource.tileTopLeft)))
        lazy val basePaired = baseOpposite.orElse(baseRemote)

        def threatenedAt(atResource: UnitInfo): Boolean = unit.matchups.threats.exists(threat =>
          ! threat.isAny(MatchWorkers, Terran.Wraith, Protoss.Arbiter, Protoss.Scout, Zerg.Mutalisk)
            && threat.pixelsToGetInRange(unit, atResource.pixel) < defenseRadiusPixels)

        if (basePaired.exists(_.owner.isUs) && threatenedAt(resource)) {
          val alternativeMineral = ByOption.minBy(basePaired.get.minerals.filter(_.alive))(_.pixelDistanceEdge(unit)).orElse(unit.agent.toGather)
          unit.agent.toGather = alternativeMineral.filterNot(threatenedAt).orElse(Some(resource))
        }
      }

      // Burrow from threats
      if (unit.canBurrow
        && unit.matchups.enemies.exists(enemy =>
          ! enemy.isAny(MatchWorkers, Terran.Wraith, Protoss.Arbiter, Protoss.Scout)
          && enemy.pixelDistanceEdge(unit) < enemy.pixelRangeAgainst(unit) + 32)
          && ! unit.tile.enemyDetected) {
        Commander.burrow(unit)
      }

      // Help with fights when appropriate
      lazy val beckonedToFight  = unit.matchups.targets.exists(target =>
        ! target.unitClass.isWorker
        && (target.canAttack || target.unitClass.rawCanAttack)
        && target.pixelDistanceCenter(unit)     < defenseRadiusPixels
        && target.pixelDistanceCenter(resource) < defenseRadiusPixels)
      if (unit.totalHealth > 32 && beckonedToFight) {
        Target.choose(unit)
        Commander.attack(unit)
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
          Commander.returnCargo(unit)
        } else if (bestGoal.isDefined) {
          // If we expect to die anyway
          if (unit.pixelDistanceEdge(bestGoal.get) < 32) {
            Potshot.consider(unit)
          } else {
            unit.agent.toGather = bestGoal
            Commander.gather(unit)
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
      lazy val threatCloser     = unit.matchups.threats.exists(_.pixelDistanceCenter(resource.pixel) < unit.pixelDistanceCenter(resource.pixel))
      if (transferring
        && threatened
        && threatCloser
        && (unit.visibleToOpponents || ! unit.agent.withinSafetyMargin || unit.zone.edges.exists(_.contains(unit.pixel)))) {
        Retreat.consider(unit)
      }
    }

    // Benzene travel hack
    if (Benzene.matches && resource.zone != unit.zone && unit.zone == With.geography.ourMain.zone) {
      Commander.move(unit)
    }
    
    Commander.gather(unit)
  }
}
