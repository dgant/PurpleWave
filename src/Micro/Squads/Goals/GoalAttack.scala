package Micro.Squads.Goals

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Micro.Squads.SquadBatch
import Planning.UnitMatchers.{UnitMatchProxied, UnitMatchWarriors}
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

class GoalAttack extends SquadGoalBasic {

  override def inherentValue: Double = GoalValue.attack

  override def candidateValue(batch: SquadBatch, candidate: FriendlyUnitInfo): Double = {
    PurpleMath.clamp(super.candidateValue(batch, candidate), 0.5, 5.0)
  }
  
  override def toString: String = "Attack " + target.zone.name
  
  var target: Pixel = With.scouting.mostBaselikeEnemyTile.pixelCenter
  
  override def run() {
    chooseTarget()
    squad.units.foreach(attacker => {
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
    squad.enemies = With.units.enemy.view.filter(u => u.possiblyStillThere && targetFilter(u) && u.zone == target.zone || u.canMove || u.unitClass.isSiegeTank).toSeq
  }
  
  protected def chooseTarget(): Unit = {
    val focusEnemy = With.scouting.threatOrigin
    val focusUs = PurpleMath.centroid(squad.units.view.map(_.pixelCenter)).tileIncluding
    val threatDistanceToUs =
      ByOption.min(With.geography.ourBases.map(_.heart.tileDistanceFast(focusEnemy)))
        .getOrElse(With.geography.home.tileDistanceFast(focusEnemy))
    val threatDistanceToEnemy =
      ByOption.min(With.geography.enemyBases.map(_.heart.tileDistanceFast(focusUs)))
        .getOrElse(With.scouting.mostBaselikeEnemyTile.tileDistanceFast(focusUs))

    lazy val enemyNonTrollyThreats = With.units.enemy.count(u => u.is(UnitMatchWarriors) && u.possiblyStillThere && ! u.is(Terran.Vulture) && u.detected)
    if (With.enemies.exists( ! _.isZerg)
      && With.enemy.bases.size < 3
      && threatDistanceToUs < threatDistanceToEnemy
      && enemyNonTrollyThreats > 6) {
      target = With.scouting.threatOrigin.pixelCenter
      return
    }
    target =
      ByOption.minBy(With.geography.ourBasesAndSettlements.flatMap(_.units.filter(u => u.isEnemy && u.unitClass.isBuilding).map(_.pixelCenter)))(_.groundPixels(With.geography.home.pixelCenter))
      .orElse(
        if (With.geography.ourBases.size > 1 && With.frame > GameTime(10, 0)())
          None
        else
          ByOption.minBy(With.units.enemy.view.filter(_.is(UnitMatchProxied)).map(_.pixelCenter))(_.groundPixels(With.geography.home.pixelCenter)))
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
            .map(_.pixelCenter)
            .getOrElse(base.townHallArea.midPixel)))
      .orElse(if (enemyNonTrollyThreats > 0) Some(With.scouting.threatOrigin.pixelCenter) else None)
      .getOrElse(With.scouting.mostBaselikeEnemyTile.pixelCenter)
  }
}
