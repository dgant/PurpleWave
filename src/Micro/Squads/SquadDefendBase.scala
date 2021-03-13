package Micro.Squads

import Information.Geography.Types.{Base, Edge, Zone}
import Lifecycle.With
import Mathematics.Formations.{FormationAssigned, FormationZone}
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Actions.Combat.Targeting.Filters.TargetFilterDefend
import Micro.Agency.Intention
import Performance.Cache
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class SquadDefendBase(base: Base) extends Squad {

  private var lastAction = "Defend"
  override def toString: String = f"$lastAction ${base.heart}"

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
          val altitudeDiff = PurpleMath.signum(step.to.centroid.altitude - step.from.centroid.altitude)
          val altitudeMult = Math.pow(altitudeValue, altitudeDiff)
          val width = PurpleMath.clamp(step.edge.radiusPixels, 32 * 3, 32 * 16)
          val score = width * turtlePenalty * (3 + i) * altitudeMult
          (step, score)
        })
        val scoreBest = ByOption.minBy(stepScores)(_._2)
        scoreBest.foreach(s => output = (s._1.from, Some(s._1.edge)))
      })
    }
    output
  })
  private def zone: Zone = zoneAndChoke()._1
  private def choke: Option[Edge] = zoneAndChoke()._2

  override def run() {
    if (units.isEmpty) return

    lazy val allowWandering = With.geography.ourBases.size > 2 || ! With.enemies.exists(_.isZerg) || enemies.exists(_.unitClass.ranged) || With.blackboard.wantToAttack()
    lazy val canScour = scourables().nonEmpty
    lazy val canDefendChoke = choke.isDefined && (units.size > 3 || ! With.enemies.exists(_.isZerg))
    lazy val entranceBreached = scourables().exists(e =>
      e.unitClass.attacksGround
      && ! e.unitClass.isWorker
      && e.base.exists(_.owner.isUs)
      && ! zone.edges.exists(edge => e.pixelDistanceCenter(edge.pixelCenter) < 64 + edge.radiusPixels))

    if (canScour && (allowWandering || entranceBreached)) {
      lastAction = "Scour"
      huntEnemies()
    } else if ( ! entranceBreached && canDefendChoke) {
      lastAction = "Protect choke of"
      defendChoke()
    } else {
      lastAction = "Protect heart of"
      defendHeart(base.heart.pixelCenter)
    }
  }

  private def huntableFilter(enemy: UnitInfo): Boolean = (
    ! (enemy.is(Zerg.Drone) && With.fingerprints.fourPool.matches) // Don't get baited by 4-pool scouts
      && (units.exists(_.canAttack(enemy)) || (enemy.cloaked && units.exists(_.unitClass.isDetector)))
      && (enemy.matchups.targets.nonEmpty || enemy.matchups.allies.forall(_.matchups.targets.isEmpty)) // Don't, for example, chase Overlords that have ally Zerglings nearby
      // If we don't really want to fight, wait until they push into the base
      && zone.exit.forall(exit =>
        enemy.flying
          || With.blackboard.wantToAttack()
          || enemy.pixelDistanceTravelling(zone.centroid)
          < (exit.endPixels ++ exit.sidePixels :+ exit.pixelCenter).map(_.groundPixels(zone.centroid)).min))

  private val scourables = new Cache(() => {
    val huntableInZone = enemies.filter(e => e.zone == zone && huntableFilter(e)) ++ zone.units.filter(u => u.isEnemy && u.unitClass.isGas)
    if (huntableInZone.nonEmpty) huntableInZone else enemies.filter(huntableFilter)
  })

  private def huntEnemies() {
    lazy val home = ByOption.minBy(zone.bases.filter(_.owner.isUs).map(_.heart))(_.groundPixels(zone.centroid))
      .orElse(ByOption.minBy(With.geography.ourBases.map(_.heart))(_.groundPixels(zone.centroid)))
      .getOrElse(With.geography.home)
      .pixelCenter

    def distance(enemy: UnitInfo): Double = enemy.pixelDistanceSquared(base.heart.pixelCenter)

    val huntables = scourables()
    lazy val target = huntables.minBy(distance)
    lazy val targetAir = ByOption.minBy(huntables.filter(_.flying))(distance).getOrElse(target)
    lazy val targetGround = ByOption.minBy(huntables.filterNot(_.flying))(distance).getOrElse(target)
    units.foreach(recruit => {
      val onlyAir     = recruit.canAttack && ! recruit.unitClass.attacksGround
      val onlyGround  = recruit.canAttack && ! recruit.unitClass.attacksAir
      val thisTarget  = if (onlyAir) targetAir else if (onlyGround) targetGround else target
      recruit.agent.intend(this, new Intention {
        toTravel = Some(thisTarget.pixel)
        targetFilters = Seq(TargetFilterDefend(zone))
      })
    })
  }

  private def defendHeart(center: Pixel) {
    val protectables  = center.zone.units.filter(u => u.isOurs && u.unitClass.isBuilding && u.hitPoints < 300 && (u.friendly.exists(_.knownToEnemy) || u.canAttack))
    val destination   = ByOption
      .minBy(protectables.view.filter(p =>
        p.zone != With.geography.ourMain.zone || p.matchups.threats.exists( ! _.unitClass.isWorker)))(u =>
          u.matchups.framesOfSafety + 0.0001 * u.pixelDistanceCenter(center))
      .map(_.pixel)
      .getOrElse(center)
    val groupArea     = units.view.map(_.unitClass.area).sum
    val groupRadius   = Math.sqrt(groupArea)
    units.foreach(unit => {
      val unitDestination = if (unit.flying || unit.pixelDistanceCenter(destination) > groupRadius) destination else unit.pixel
      unit.agent.intend(this, new Intention {
        toTravel = Some(unitDestination)
        toReturn = if (zone.bases.exists(_.owner.isUs)) Some(unitDestination) else None
        targetFilters = Seq(TargetFilterDefend(zone))
      })})
  }

  private def defendChoke() {
    assignToFormation(new FormationZone(zone, choke.get).form(units.toSeq))
  }

  private def assignToFormation(formation: FormationAssigned): Unit = {
    units.foreach(
      defender => {
        val spot = formation.placements.get(defender)
        defender.agent.intend(this, new Intention {
          toReturn = spot
          toTravel = spot.orElse(Some(zone.centroid.pixelCenter))
          targetFilters = Seq(TargetFilterDefend(zone))
        })
      })
  }
}
