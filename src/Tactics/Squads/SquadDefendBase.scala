package Tactics.Squads

import Information.Geography.Types.{Base, Edge, Zone}
import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Micro.Formation.{Formation, FormationEmpty, FormationGeneric, FormationZone}
import Performance.Cache
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo

class SquadDefendBase(base: Base) extends Squad {

  vicinity = base.heart.center

  private var lastAction = "Def"
  override def toString: String = f"$lastAction ${base.name.take(5)}"

  val zoneAndChoke = new Cache(() => {
    val zone: Zone = base.zone
    val threat = With.scouting.threatOrigin.zone
    var output = (zone, zone.exitNow)
    if ( ! threat.bases.exists(_.owner.isUs)) {
      val possiblePath = With.paths.zonePath(zone, threat)
      possiblePath.foreach(path => {
        val stepScores = path.steps.take(4).filter(_.from.centroid.tileDistanceManhattan(With.geography.home) < 72).indices.map(i => {
          val step = path.steps(i)
          val turtlePenalty = if (step.to.units.exists(u => u.isOurs && u.unitClass.isBuilding)) 10 else 1
          val altitudeValue = if (With.enemies.forall(_.isZerg)) 1 else 5
          val altitudeDiff = Maff.signum(step.to.centroid.altitude - step.from.centroid.altitude)
          val altitudeMult = Math.pow(altitudeValue, altitudeDiff)
          val width = Maff.clamp(step.edge.radiusPixels, 32 * 3, 32 * 16)
          val score = width * turtlePenalty * (3 + i) * altitudeMult
          (step, score)
        })
        val scoreBest = Maff.minBy(stepScores)(_._2)
        scoreBest.foreach(s => output = (s._1.from, Some(s._1.edge)))
      })
    }
    output
  })
  private def guardZone: Zone = zoneAndChoke()._1
  private def guardChoke: Option[Edge] = zoneAndChoke()._2
  lazy val enemyHasVision: Cache[Boolean] = new Cache(() => enemies.exists(e => e.flying || e.altitude >= base.heart.altitude))
  lazy val heart = if (base.metro == With.geography.ourMain.metro) With.geography.ourMain.heart.center else base.heart.center
  val bastion = new Cache(() =>
    Maff.minBy(
        base.units.view.filter(u =>
          u.isOurs
          && u.unitClass.isBuilding
          && u.hitPoints < 300
          && (u.friendly.exists(_.knownToEnemy) || u.canAttack)
          && (u.zone != With.geography.ourMain.zone || u.matchups.threats.exists( ! _.unitClass.isWorker))))(u => u.matchups.framesOfSafety + 0.0001 * u.pixelDistanceCenter(heart))
      .map(_.pixel)
      .getOrElse(heart))

  private var formationReturn: Formation = FormationEmpty

  override def run() {
    formation = None
    if (units.isEmpty) return
    targetQueue = Some(SquadTargeting.rankForArmy(units, enemies))
    lazy val scourables   = enemies.filter(isHuntable)
    lazy val canScour     = scourables.nonEmpty && (wander || breached)
    lazy val wander       = With.geography.ourBases.size > 2 || ! With.enemies.exists(_.isZerg) || With.blackboard.wantToAttack()
    lazy val canGuard     = guardChoke.isDefined && (units.size > 3 || ! With.enemies.exists(_.isZerg))
    lazy val breached     = scourables.exists(e =>
      e.canAttackGround
      && ! e.unitClass.isWorker
      && e.base.exists(_.owner.isUs)
      && ! guardZone.edges.exists(edge => e.pixelDistanceCenter(edge.pixelCenter) < 64 + edge.radiusPixels))

    lazy val formationBastion = FormationGeneric.march(units, bastion())
    lazy val formationGuard = guardChoke.map(c => FormationZone(units, guardZone, c)).getOrElse(formationBastion)

    if (canScour) {
      lastAction = "Scour"
      formation = Some(FormationGeneric.engage(units, targetQueue.get.headOption.map(_.pixel)))
      formationReturn = formationGuard
    } else if (canGuard) {
      lastAction = "Guard"
      formation = Some(formationGuard)
      formationReturn = formationGuard
    } else {
      lastAction = "Hold"
      formation = Some(formationBastion)
      formationReturn = formationBastion
    }
    val targets = if (canScour) scourables else enemies.filter(threateningBase)
    intendFormation()
  }

  private def isHuntable(enemy: UnitInfo): Boolean = (
    ! (enemy.is(Zerg.Drone) && With.fingerprints.fourPool.matches) // Don't get baited by 4-pool scouts
    && (units.exists(_.canAttack(enemy)) || (enemy.cloaked && units.exists(_.unitClass.isDetector)))
    && (enemy.matchups.targets.nonEmpty || enemy.matchups.allies.forall(_.matchups.targets.isEmpty)) // Don't, for example, chase Overlords that have ally Zerglings nearby
    // If we don't really want to fight, wait until they push into the base
    && (
      enemy.flying
      || With.blackboard.wantToAttack()
      || (enemy.base.contains(base) && ! base.zone.exit.exists(_.contains(enemy.pixel)))))

  private def threateningBase(enemy: UnitInfo): Boolean = {
    if (enemy.zone == base.zone) return true
    // If they're between the bastion and the base
    if ( ! enemy.flying && enemy.pixelDistanceTravelling(base.zone.centroid) < bastion().groundPixels(base.zone.centroid)) return true
    // If they can assault our base from outside it
    if (enemyHasVision() && base.zone.units.view.filter(enemy.inRangeToAttack).exists(u => u.unitClass.melee || ! base.zone.edges.exists(_.contains(u.pixel)))) return true
    false
  }
  private def intendFormation(): Unit = {
    units.foreach(unit => {
      unit.intend(this, new Intention {
        toTravel = Some(formation.get(unit))
        toReturn = Some(formationReturn(unit))
      })
    })
  }
}
