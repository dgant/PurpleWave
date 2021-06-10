package Micro.Squads

import Debugging.Decap
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Micro.Formation.FormationGeneric
import Planning.UnitMatchers.{MatchProxied, MatchWarriors}
import ProxyBwapi.Races.Terran
import Utilities.{ByOption, Minutes}

class SquadAttack extends Squad {
  override def toString: String = f"Attack ${Decap(vicinity.base.getOrElse(vicinity.zone))}"

  override def run() {
    chooseVicinity()
    if (units.isEmpty) return
    val fightConsensus = PurpleMath.mode(units.view.map(_.agent.shouldEngage))
    formation = Some(if (fightConsensus) {
      FormationGeneric.march(units, vicinity)
    } else {
      FormationGeneric.disengage(units)
    })

    units.foreach(attacker => {
      attacker.agent.intend(this, new Intention {
        toTravel = formation.get.placements.get(attacker).orElse(Some(vicinity))
      })
    })
  }

  protected def chooseVicinity(): Unit = {
    val threatOrigin = With.scouting.threatOrigin
    val centroid = PurpleMath.centroid(units.view.map(_.pixel)).tile
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
