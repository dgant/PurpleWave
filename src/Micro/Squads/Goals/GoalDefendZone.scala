package Micro.Squads.Goals

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Formations.Designers.FormationZone
import Mathematics.Formations.FormationAssigned
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Actions.Combat.Targeting.Filters.TargetFilterDefend
import Micro.Agency.Intention
import Performance.Cache
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class GoalDefendZone extends SquadGoalBasic {

  override def inherentValue: Double = GoalValue.defendBase

  private var lastAction = "Defend "

  override def toString: String = lastAction + zone
  override def destination: Pixel = _currentDestination

  var zone: Zone = _
  private var _currentDestination: Pixel = Pixel(0, 0)

  override def run() {
    lazy val base = ByOption.minBy(zone.bases)(_.heart.tileDistanceManhattan(With.scouting.threatOrigin))
    _currentDestination = zone.centroid.pixelCenter.nearestWalkableTile.pixelCenter

    if (squad.units.isEmpty) return

    lazy val choke = zone.exit
    lazy val walls = zone.units.filter(u =>
      u.isOurs
        && u.unitClass.isStaticDefense
        && ( ! u.is(Protoss.ShieldBattery) || choke.forall(_.pixelCenter.pixelDistance(u.pixel) > 96 + u.effectiveRangePixels))
        && (squad.enemies.isEmpty || squad.enemies.exists(u.canAttack)))

    lazy val allowWandering = With.geography.ourBases.size > 2 || ! With.enemies.exists(_.isZerg) || squad.enemies.exists(_.unitClass.ranged) || With.blackboard.wantToAttack()
    lazy val canHuntEnemies = huntableEnemies().nonEmpty
    lazy val canDefendChoke = (squad.units.size > 3 && choke.isDefined) || ! With.enemies.exists(_.isZerg)
    lazy val wallExistsButNoneNearChoke = walls.nonEmpty && walls.forall(wall =>
      choke.forall(chokepoint =>
        (chokepoint.sidePixels :+ chokepoint.pixelCenter).forall(wall.pixelDistanceCenter(_) > 8 * 32)))
    lazy val entranceBreached = huntableEnemies().exists(e =>
      e.attacksAgainstGround > 0
      && ! e.unitClass.isWorker
      && e.zone == zone
      && ! zone.edges.exists(edge => e.pixelDistanceCenter(edge.pixelCenter) < 64 + edge.radiusPixels))

    if ((allowWandering || entranceBreached) && canHuntEnemies) {
      lastAction = "Scour "
      huntEnemies()
    }
    else if (wallExistsButNoneNearChoke) {
      lastAction = "Protect wall of "
      defendHeart(walls.minBy(_.pixel.groundPixels(With.scouting.mostBaselikeEnemyTile)).pixel)
    }
    else if (! entranceBreached && canDefendChoke) {
      lastAction = "Protect choke of "
      defendChoke()
    }
    else {
      lastAction = "Protect heart of "
      defendHeart(base.map(_.heart.pixelCenter).getOrElse(zone.centroid.pixelCenter))
    }
  }

  private val pointsOfInterest = new Cache[Vector[Pixel]](() => {
    val output = (
      zone.bases.filter(_.owner.isUs).map(_.heart.pixelCenter)
        ++ zone.units.view.filter(u => u.isOurs && u.unitClass.isBuilding).map(_.pixel).toVector
      ).take(10)
    if (output.isEmpty) Vector(zone.centroid.pixelCenter) else output
  } // For performance
  )

  private def huntableFilter(enemy: UnitInfo): Boolean = (
    ! (enemy.is(Zerg.Drone) && With.fingerprints.fourPool.matches) // Don't get baited by 4-pool scouts
      && (squad.units.exists(_.canAttack(enemy)) || (enemy.cloaked && squad.units.exists(_.unitClass.isDetector)))
      && (enemy.matchups.targets.nonEmpty || enemy.matchups.allies.forall(_.matchups.targets.isEmpty)) // Don't, for example, chase Overlords that have ally Zerglings nearby
      // If we don't really want to fight, wait until they push into the base
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
      ByOption.min(pointsOfInterest().map(_.pixelDistance(enemy.pixel))).getOrElse(enemy.pixelDistanceCenter(home))
    }

    val huntables = huntableEnemies()
    _currentDestination = PurpleMath.centroid(huntables.view.map(_.pixel))

    lazy val target = huntables.minBy(distance)
    lazy val targetAir = ByOption.minBy(huntables.filter(_.flying))(distance).getOrElse(target)
    lazy val targetGround = ByOption.minBy(huntables.filterNot(_.flying))(distance).getOrElse(target)
    squad.units.foreach(recruit => {
      val onlyAir     = recruit.canAttack && !recruit.unitClass.attacksGround
      val onlyGround  = recruit.canAttack && !recruit.unitClass.attacksAir
      val thisTarget  = if (onlyAir) targetAir else if (onlyGround) targetGround else target
      recruit.agent.intend(squad.client, new Intention {
        toTravel = Some(thisTarget.pixel)
        targetFilters = Seq(TargetFilterDefend(zone))
      })
    })
  }

  def defendHeart(center: Pixel) {
    _currentDestination = center
    val protectables  = center.zone.units.filter(u => u.isOurs && u.unitClass.isBuilding && u.hitPoints < 300 && (u.friendly.exists(_.knownToEnemy) || u.canAttack))
    val destination   = ByOption
      .minBy(protectables.view.filter(p =>
        p.zone != With.geography.ourMain.zone || p.matchups.threats.exists( ! _.unitClass.isWorker)))(u =>
          u.matchups.framesOfSafety + 0.0001 * u.pixelDistanceCenter(center))
      .map(_.pixel)
      .getOrElse(center)
    val groupArea     = squad.units.view.map(_.unitClass.area).sum
    val groupRadius   = Math.sqrt(groupArea)
    squad.units.foreach(unit => {
      val unitDestination = if (unit.flying || unit.pixelDistanceCenter(destination) > groupRadius) destination else unit.pixel
      unit.agent.intend(squad.client, new Intention {
        toTravel = Some(unitDestination)
        toReturn = if (zone.bases.exists(_.owner.isUs)) Some(unitDestination) else None
        targetFilters = Seq(TargetFilterDefend(zone))
      })})
  }

  def defendChoke() {
    _currentDestination = zone.exit.map(_.pixelCenter).getOrElse(zone.centroid.pixelCenter)
    assignToFormation(new FormationZone(zone, squad.enemies).form(squad.units.toSeq))
  }

  def assignToFormation(formation: FormationAssigned): Unit = {
    squad.units.foreach(
      defender => {
        val spot = formation.placements.get(defender)
        defender.agent.intend(squad.client, new Intention {
          toReturn = spot
          toTravel = spot.orElse(Some(zone.centroid.pixelCenter))
          targetFilters = Seq(TargetFilterDefend(zone))
        })
      })
  }
}
