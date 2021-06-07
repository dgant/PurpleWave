package Micro.Squads

import Debugging.Decap
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Micro.Squads.Formation.NewFormation
import Planning.UnitMatchers.{MatchProxied, MatchWarriors}
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.{ByOption, Minutes}

class SquadAttack extends Squad {
  override def toString: String = f"Attack ${Decap(vicinity.base.getOrElse(vicinity.zone))}"

  override def run() {
    chooseVicinity()
    formation = Some(NewFormation.march(units, vicinity))
    units.foreach(attacker => {
      attacker.agent.intend(this, new Intention {
        toTravel = formation.get.placements.get(attacker).orElse(Some(vicinity))
      })
    })

    def vicinityFilter(unit: UnitInfo): Boolean = (
      unit.isEnemy
      && unit.alive
      && unit.likelyStillThere
      && unit.unitClass.dealsDamage)

    val occupiedBases = units.flatMap(_.base).filter(_.owner.isEnemy)
  }

  protected def chooseVicinity(): Unit = {
    val focusEnemy = With.scouting.threatOrigin
    val focusUs = PurpleMath.centroid(units.view.map(_.pixel)).tile
    val threatDistanceToUs =
      ByOption.min(With.geography.ourBases.map(_.heart.tileDistanceFast(focusEnemy)))
        .getOrElse(With.geography.home.tileDistanceFast(focusEnemy))
    val threatDistanceToEnemy =
      ByOption.min(With.geography.enemyBases.map(_.heart.tileDistanceFast(focusUs)))
        .getOrElse(With.scouting.mostBaselikeEnemyTile.tileDistanceFast(focusUs))

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
