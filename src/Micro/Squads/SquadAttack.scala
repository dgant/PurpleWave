package Micro.Squads

import Debugging.Decap
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Planning.UnitMatchers.{MatchProxied, MatchWarriors}
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.{ByOption, Minutes}

class SquadAttack extends Squad {
  override def toString: String = f"Attack ${Decap(target.base.getOrElse(target.zone))}"

  var target: Pixel = With.scouting.mostBaselikeEnemyTile.pixelCenter

  override def run() {
    chooseTarget()
    units.foreach(attacker => {
      attacker.agent.intend(this, new Intention {
        toTravel = Some(target)
      })
    })

    def targetFilter(unit: UnitInfo): Boolean = (
      unit.isEnemy
      && unit.alive
      && unit.likelyStillThere
      && unit.unitClass.dealsDamage)

    val occupiedBases = units.flatMap(_.base).filter(_.owner.isEnemy)
  }

  protected def chooseTarget(): Unit = {
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
      target = With.scouting.threatOrigin.pixelCenter
      return
    }
    target =
      ByOption.minBy(With.geography.ourBasesAndSettlements.flatMap(_.units.filter(u => u.isEnemy && u.unitClass.isBuilding).map(_.pixel)))(_.groundPixels(With.geography.home.pixelCenter))
      .orElse(
        if (With.geography.ourBases.size > 1 && With.frame > Minutes(10)())
          None
        else
          ByOption.minBy(With.units.enemy.view.filter(MatchProxied).map(_.pixel))(_.groundPixels(With.geography.home.pixelCenter)))
      .orElse(
        ByOption
          .maxBy(With.geography.enemyBases)(base => {
            val distance      = With.scouting.threatOrigin.pixelCenter.pixelDistance(base.heart.pixelCenter)
            val distanceLog   = 1 + Math.log(1 + distance)
            val defendersLog  = 1 + Math.log(1 + base.defenseValue)
            val output        = distanceLog / defendersLog
            output
          })
          .map(base => ByOption.minBy(base.units.filter(u => u.isEnemy && u.unitClass.isBuilding))(_.pixelDistanceCenter(base.townHallArea.midPixel))
            .map(_.pixel)
            .getOrElse(base.townHallArea.midPixel)))
      .orElse(if (enemyNonTrollyThreats > 0) Some(With.scouting.threatOrigin.pixelCenter) else None)
      .getOrElse(With.scouting.mostBaselikeEnemyTile.pixelCenter)
  }
}
