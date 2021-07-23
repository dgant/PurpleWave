package Tactics.Squads

import Information.Geography.Types.{Base, Edge, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Formation.{Formation, FormationEmpty, FormationGeneric, FormationZone}
import Performance.Cache
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo

class SquadDefendBase(base: Base) extends Squad {

  override def launch(): Unit = { /* This squad is given its recruits externally */ }

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
  lazy val heart: Pixel = if (base.metro == With.geography.ourMain.metro) With.geography.ourMain.heart.center else base.heart.center
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
    if (units.isEmpty) return
    lazy val scourables   = enemies.filter(isHuntable)
    lazy val canScour     = scourables.nonEmpty && (wander || breached)
    lazy val wander       = With.geography.ourBases.size > 2 || ! With.enemies.exists(_.isZerg) || With.blackboard.wantToAttack()
    lazy val canGuard     = guardChoke.isDefined && (units.size > 3 || ! With.enemies.exists(_.isZerg))
    lazy val breached     = scourables.exists(e =>
      e.canAttackGround
      && ! e.unitClass.isWorker
      && e.base.exists(_.owner.isUs)
      && ! guardZone.edges.exists(edge => e.pixelDistanceCenter(edge.pixelCenter) < 64 + edge.radiusPixels))

    val targets = if (canScour) scourables else enemies.filter(threateningBase)
    targetQueue = Some(SquadAutomation.rankForArmy(this, targets))
    lazy val formationScour = FormationGeneric.engage(this, targetQueue.get.headOption.map(_.pixel))
    lazy val formationBastion = FormationGeneric.march(this, bastion())
    lazy val formationGuard = guardChoke.map(c => FormationZone(this, guardZone, c)).getOrElse(formationBastion)

    formations.clear()
    if (canScour) {
      lastAction = "Scour"
      formations += formationScour
      formations += formationGuard
    } else if (canGuard) {
      lastAction = "Guard"
      formations += formationGuard
    } else {
      lastAction = "Hold"
      formations += formationBastion
    }
    SquadAutomation.send(this)
  }

  private def isHuntable(enemy: UnitInfo): Boolean = (
    ! (Zerg.Drone(enemy) && With.fingerprints.fourPool.matches) // Don't get baited by 4-pool scouts
    && (units.exists(_.canAttack(enemy)) || (enemy.cloaked && units.exists(_.unitClass.isDetector)))
    && (enemy.matchups.targets.nonEmpty || enemy.matchups.allies.forall(_.matchups.targets.isEmpty)) // Don't, for example, chase Overlords that have ally Zerglings nearby
    // If we don't really want to fight, wait until they push into the base
    && (
      enemy.flying
      || With.blackboard.wantToAttack()
      || (enemy.base.contains(base) && ! base.zone.exit.exists(_.contains(enemy.pixel)))))

  private val enemyHasVision: Cache[Boolean] = new Cache(() => enemies.exists(e => e.flying || e.altitude >= base.heart.altitude))
  private def threateningBase(enemy: UnitInfo): Boolean = {
    if (enemy.zone == base.zone) return true
    // If they're between the bastion and the base
    if ( ! enemy.flying && enemy.pixelDistanceTravelling(base.zone.centroid) < bastion().groundPixels(base.zone.centroid)) return true
    // If they can assault our base from outside it
    if (enemyHasVision() && base.zone.units.view.filter(enemy.inRangeToAttack).exists(u => u.unitClass.melee || ! base.zone.edges.exists(_.contains(u.pixel)))) return true
    false
  }
}
