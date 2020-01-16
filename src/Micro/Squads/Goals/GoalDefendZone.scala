package Micro.Squads.Goals

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Formations.Designers.FormationZone
import Mathematics.Formations.FormationAssigned
import Mathematics.Points.Pixel
import Micro.Agency.{Intention, Leash}
import Performance.Cache
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class GoalDefendZone extends GoalBasic {

  private var lastAction = "Defend "

  override def toString: String = lastAction + zone

  var zone: Zone = _

  override def run() {
    if (squad.units.isEmpty) return

    lazy val base = ByOption.minBy(zone.bases)(_.heart.tileDistanceManhattan(With.intelligence.threatOrigin))
    lazy val choke = zone.exit
    lazy val walls = zone.units.filter(u =>
      u.isOurs
        && u.unitClass.isStaticDefense
        && ( ! u.is(Protoss.ShieldBattery) || choke.forall(_.pixelCenter.pixelDistance(u.pixelCenter) > 96 + u.effectiveRangePixels))
        && (squad.enemies.isEmpty || squad.enemies.exists(u.canAttack)))

    lazy val allowWandering = With.geography.ourBases.size > 2 || !With.enemies.exists(_.isZerg) || squad.units.size > 3 || squad.enemies.exists(_.unitClass.ranged)
    lazy val canHuntEnemies = huntableEnemies().nonEmpty
    lazy val canDefendChoke = choke.isDefined
    lazy val wallExistsNotNearChoke = walls.exists(wall => choke.forall(chokepoint => wall.pixelDistanceCenter(chokepoint.pixelCenter) > 8 * 32))

    if (allowWandering && canHuntEnemies) {
      lastAction = "Scour "
      huntEnemies()
    }
    else if (wallExistsNotNearChoke) {
      lastAction = "Protect wall of "
      defendHeart(walls.minBy(_.pixelCenter.groundPixels(With.intelligence.mostBaselikeEnemyTile)).pixelCenter)
    }
    else if (allowWandering && canDefendChoke) {
      lastAction = "Protect choke of "
      defendChoke()
    }
    else {
      lastAction = "Protect heart of "
      defendHeart(base.map(_.heart.pixelCenter).getOrElse(zone.centroid.pixelCenter))
    }
  }

  private val pointsOfInterest = new Cache[Array[Pixel]](() => {
    val output = (
      zone.bases.filter(_.owner.isUs).map(_.heart.pixelCenter)
        ++ zone.units.view.filter(u => u.isOurs && u.unitClass.isBuilding).map(_.pixelCenter).toVector
      ).take(10)
    if (output.isEmpty) Array(zone.centroid.pixelCenter) else output
  } // For performance
  )

  private def huntableFilter(enemy: UnitInfo): Boolean = (
    ! (enemy.is(Zerg.Drone) && With.fingerprints.fourPool.matches)
      && (
        squad.units.exists(_.canAttack(enemy))
        || (enemy.cloaked && squad.units.exists(_.unitClass.isDetector)))
      && (enemy.matchups.targets.nonEmpty || enemy.matchups.allies.forall(_.matchups.targets.isEmpty)) // Don't, for example, chase Overlords that have ally Zerglings nearby
      && zone.exit.forall(exit =>
      enemy.flying
        || With.blackboard.wantToAttack()
        || enemy.pixelDistanceTravelling(zone.centroid)
        < (exit.endPixels ++ exit.sidePixels :+ exit.pixelCenter).map(_.groundPixels(zone.centroid)).min))

  private val huntableEnemies = new Cache(() => {
    val huntableInZone = squad.enemies.filter(e => e.zone == zone && huntableFilter(e)) ++ zone.units.filter(u => u.isEnemy && u.unitClass.isGas)
    if (huntableInZone.nonEmpty) huntableInZone else squad.enemies.filter(huntableFilter)
  })

  def huntEnemies() {
    lazy val home = ByOption.minBy(zone.bases.filter(_.owner.isUs).map(_.heart))(_.groundPixels(zone.centroid))
      .orElse(ByOption.minBy(With.geography.ourBases.map(_.heart))(_.groundPixels(zone.centroid)))
      .getOrElse(With.geography.home)
      .pixelCenter

    def distance(enemy: UnitInfo): Double = {
      ByOption.min(pointsOfInterest().map(_.pixelDistance(enemy.pixelCenter))).getOrElse(enemy.pixelDistanceCenter(home))
    }

    lazy val target = huntableEnemies().minBy(distance)
    lazy val targetAir = ByOption.minBy(huntableEnemies().filter(_.flying))(distance).getOrElse(target)
    lazy val targetGround = ByOption.minBy(huntableEnemies().filterNot(_.flying))(distance).getOrElse(target)
    squad.units.foreach(recruit => {
      val onlyAir = recruit.canAttack && !recruit.unitClass.attacksGround
      val onlyGround = recruit.canAttack && !recruit.unitClass.attacksAir
      val thisTarget = if (onlyAir) targetAir else if (onlyGround) targetGround else target
      recruit.agent.intend(squad.client, new Intention {
        canFocus = true
        toTravel = Some(thisTarget.pixelCenter)
      })
    })
  }

  def defendHeart(center: Pixel) {
    val protectables = center.zone.units.filter(u => u.isOurs && u.unitClass.isBuilding && u.hitPoints < 300 && (u.discoveredByEnemy || u.canAttack))
    val protectRange = ByOption.max(protectables.map(_.pixelDistanceCenter(center))).getOrElse(32.0 * 8.0)
    val destination = ByOption.minBy(protectables)(_.matchups.framesOfSafety).map(_.pixelCenter).getOrElse(center)
    squad.units.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(destination)
      toReturn = if (zone.bases.exists(_.owner.isUs)) Some(destination) else None
      toLeash = Some(Leash(center, protectRange))
    }))
  }

  def defendChoke() {
    assignToFormation(new FormationZone(zone, squad.enemies).form(squad.units.toSeq))
  }

  def assignToFormation(formation: FormationAssigned): Unit = {
    squad.units.foreach(
      defender => {
        val spot = formation.placements.get(defender)
        defender.agent.intend(squad.client, new Intention {
          toForm = spot
          toReturn = spot
          toTravel = spot.orElse(Some(zone.centroid.pixelCenter))
        })
      })
  }
}
