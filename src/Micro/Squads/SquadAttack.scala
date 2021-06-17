package Micro.Squads

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Micro.Formation.FormationGeneric
import Planning.UnitMatchers.{MatchProxied, MatchWarriors}
import ProxyBwapi.Races.Terran
import Utilities.{ByOption, Minutes}

class SquadAttack extends Squad {
  override def toString: String = f"Atk ${vicinity.base.map(_.name).getOrElse(vicinity.zone.name).take(4)}"

  override def run() {
    formation = None
    chooseVicinity()
    if (units.isEmpty) return
    lazy val centroid             = Maff.centroid(units.view.map(_.pixel))
    lazy val fightConsensus       = Maff.mode(units.view.map(_.agent.shouldEngage))
    lazy val originConsensus      = Maff.mode(units.view.map(_.agent.origin))
    lazy val battleConsensus      = Maff.mode(units.view.map(_.battle))
    lazy val targetReadyToEngage  = targetQueue.get.find(t => units.exists(u => u.canAttack(t) && u.pixelsToGetInRange(t) < 64))
    lazy val targetHasEngagedUs   = targetQueue.get.find(t => units.exists(u => t.canAttack(u) && t.inRangeToAttack(u)))
    if (fightConsensus) {
      targetQueue = Some(SquadTargeting.rank(units, SquadTargeting.enRouteTo(units, vicinity)))
      if (targetReadyToEngage.isDefined || targetHasEngagedUs.isDefined) {
        formation = Some(FormationGeneric.engage(units, targetReadyToEngage.orElse(targetHasEngagedUs).map(_.pixel)))
      } else {
        formation = Some(FormationGeneric.march(units, vicinity))
      }
    } else {
      targetQueue = Some(SquadTargeting.rank(units, SquadTargeting.enRouteTo(units, originConsensus)))
      if (centroid.zone == originConsensus.zone && With.scouting.threatOrigin.zone != originConsensus.zone) {
        formation = Some(FormationGeneric.guard(units, Some(originConsensus)))
      } else {
        formation = Some(FormationGeneric.disengage(units))
      }
    }

    units.foreach(attacker => {
      attacker.agent.intend(this, new Intention {
        toTravel = formation.filter(_.placements.size > 4).flatMap(_.placements.get(attacker)).orElse(Some(vicinity))
        dropOnArrival = false
      })
    })
  }

  protected def chooseVicinity(): Unit = {
    val threatOrigin = With.scouting.threatOrigin
    val centroid = Maff.centroid(units.view.map(_.pixel)).tile
    val threatDistanceToUs =
      ByOption.min(With.geography.ourBases.map(_.heart.tileDistanceFast(threatOrigin)))
        .getOrElse(With.geography.home.tileDistanceFast(threatOrigin))
    val threatDistanceToEnemy =
      ByOption.min(With.geography.enemyBases.map(_.heart.tileDistanceFast(centroid)))
        .getOrElse(With.scouting.mostBaselikeEnemyTile.tileDistanceFast(centroid))

    lazy val enemyNonTrollyThreats = With.units.enemy.count(u => u.is(MatchWarriors) && u.likelyStillThere && ! u.is(Terran.Vulture) && u.detected)
    if (With.enemies.exists( ! _.isZerg)
      && With.enemy.bases.size < 3
      && threatDistanceToUs < threatDistanceToEnemy
      && enemyNonTrollyThreats > 6) {
      vicinity = With.scouting.threatOrigin.center
      return
    }
    vicinity =
      ByOption.minBy(With.geography.ourBasesAndSettlements.flatMap(_.units.filter(u => u.isEnemy && u.unitClass.isBuilding).map(_.pixel)))(_.groundPixels(With.geography.home.center))
      .orElse(
        if (With.geography.ourBases.size > 1 && With.frame > Minutes(10)())
          None
        else
          ByOption.minBy(With.units.enemy.view.filter(MatchProxied).map(_.pixel))(_.groundPixels(With.geography.home.center)))
      .orElse(
        ByOption
          .maxBy(With.geography.enemyBases)(base => {
            val distance      = With.scouting.threatOrigin.center.pixelDistance(base.heart.center)
            val distanceLog   = 1 + Math.log(1 + distance)
            val defendersLog  = 1 + Math.log(1 + base.defenseValue)
            val output        = distanceLog / defendersLog
            output
          })
          .map(base => base.natural.filter(_.owner == base.owner).getOrElse(base))
          .map(base => ByOption.minBy(base.units.filter(u => u.isEnemy && u.unitClass.isBuilding))(_.pixelDistanceCenter(base.townHallArea.midPixel))
            .map(_.pixel)
            .getOrElse(base.townHallArea.midPixel)))
      .orElse(if (enemyNonTrollyThreats > 0) Some(With.scouting.threatOrigin.center) else None)
      .getOrElse(With.scouting.mostBaselikeEnemyTile.center)
  }
}
