package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

class GoalAttack extends GoalBasic {
  
  override def toString: String = "Attack " + target.zone.name
  
  var target: Pixel = With.intelligence.mostBaselikeEnemyTile.pixelCenter
  
  override def run() {
    chooseTarget()
    squad.units.foreach(attacker => {
      With.intelligence.highlightScout(attacker)
      attacker.agent.intend(squad.client, new Intention {
        toTravel = Some(target)
      })
    })

    def targetFilter(unit: UnitInfo): Boolean = (
      unit.isEnemy
      && unit.likelyStillAlive
      && unit.possiblyStillThere
      && unit.unitClass.dealsDamage)

    val occupiedBases = squad.units.flatMap(_.base).filter(_.owner.isEnemy)
    squad.enemies = With.units.enemy.view.filter(u => targetFilter(u) && u.zone == target.zone || u.canMove || u.unitClass.isSiegeTank).toSeq
  }
  
  protected def chooseTarget(): Unit = {
    val focusEnemy = With.intelligence.threatOrigin
    val focusUs = squad.centroid.tileIncluding
    val threatDistanceToUs =
      ByOption.min(With.geography.ourBases.map(_.heart.tileDistanceFast(focusEnemy)))
        .getOrElse(With.geography.home.tileDistanceFast(focusEnemy))
    val threatDistanceToEnemy =
      ByOption.min(With.geography.enemyBases.map(_.heart.tileDistanceFast(focusUs)))
        .getOrElse(With.intelligence.mostBaselikeEnemyTile.tileDistanceFast(focusUs))

    lazy val enemyNonTrollyThreats = With.units.enemy.count(u => u.is(UnitMatchWarriors) && u.possiblyStillThere && ! u.is(Terran.Vulture))
    if (With.enemies.exists( ! _.isZerg)
      && threatDistanceToUs < threatDistanceToEnemy
      && enemyNonTrollyThreats > 6) {
      target = With.intelligence.threatOrigin.pixelCenter
      return
    }
    target =
      ByOption
        .maxBy(With.geography.enemyBases)(base => {
          val distance      = With.intelligence.threatOrigin.pixelCenter.pixelDistance(base.heart.pixelCenter)
          val distanceLog   = 1 + Math.log(1 + distance)
          val defendersLog  = 1 + Math.log(1 + base.defenseValue)
          val output        = distanceLog / defendersLog
          output
        })
        .map(base => ByOption.minBy(base.units.filter(u => u.isEnemy && u.unitClass.isBuilding))(_.pixelDistanceCenter(base.townHallArea.midPixel))
          .map(_.pixelCenter)
          .getOrElse(base.townHallArea.midPixel))
        .orElse(if (enemyNonTrollyThreats > 0) Some(With.intelligence.threatOrigin.pixelCenter) else None)
        .getOrElse(With.intelligence.mostBaselikeEnemyTile.pixelCenter)
  }
  
  override protected def offerUseful(candidates: Iterable[FriendlyUnitInfo]) {
    candidates.foreach(c => if (unitMatcher.accept(c)) addCandidate(c))
  }
  override protected def offerUseless(candidates: Iterable[FriendlyUnitInfo]) {}
}
