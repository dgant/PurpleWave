package Micro.Squads.Goals

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Formations.{Formation, Formations}
import Mathematics.Points.{Pixel, SpecificPoints}
import Mathematics.PurpleMath
import Micro.Agency.{Intention, Leash}
import Performance.Cache
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption

class GoalDefendZone extends GoalBasic {
  
  private var lastAction = "Defend "
  override def toString: String = lastAction + zone
  
  var zone: Zone = _
  
  override def run() {
    lazy val base   = ByOption.minBy(zone.bases)(_.heart.tileDistanceManhattan(With.intelligence.mostBaselikeEnemyTile))
    lazy val choke  = zone.exit
    lazy val walls  = zone.units.filter(u =>
      u.isOurs
      && u.unitClass.isStaticDefense
      && (squad.enemies.isEmpty || squad.enemies.exists(u.canAttack)))

    lazy val allowWandering = ! With.enemies.exists(_.isZerg) || squad.units.size >= 3 || squad.enemies.exists(_.unitClass.ranged)
    lazy val canHuntEnemies = huntableEnemies().nonEmpty
    lazy val canDefendWall  = walls.nonEmpty
    lazy val canDefendChoke = choke.isDefined
    
    if (allowWandering && canHuntEnemies) {
      lastAction = "Scour "
      huntEnemies()
    }
    else if (canDefendWall) {
      lastAction = "Protect wall of "
      defendWall(walls)
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
  
  private val huntableEnemies = new Cache(() => {
    squad.enemies.filter(enemy =>
      enemy.visible
      && (enemy.zone.edges.exists(edge => edge.zones.contains(zone)))
      && pointsOfInterest().exists(p => enemy.pixelDistanceCenter(p) < 32 * 15)
      && ! (enemy.is(Zerg.Drone) && With.fingerprints.fourPool.matches)
      && zone.edges.forall(edge => enemy.pixelDistanceCenter(edge.pixelCenter) > 32.0 * 8.0)) // Arbitrary number
  })
  
  def huntEnemies() {
    lazy val home = ByOption.minBy(zone.bases.filter(_.owner.isUs).map(_.heart))(_.groundPixels(zone.centroid))
      .orElse(ByOption.minBy(With.geography.ourBases.map(_.heart))(_.groundPixels(zone.centroid)))
      .getOrElse(With.geography.home)
      .pixelCenter

    def distance(enemy: UnitInfo): Double = {
      ByOption.min(pointsOfInterest().map(_.pixelDistance(enemy.pixelCenter))).getOrElse(enemy.pixelDistanceCenter(home))
    }
    lazy val target        = huntableEnemies().minBy(distance)
    lazy val targetAir     = ByOption.minBy(huntableEnemies().filter   (_.flying))(distance).getOrElse(target)
    lazy val targetGround  = ByOption.minBy(huntableEnemies().filterNot(_.flying))(distance).getOrElse(target)
    squad.units.foreach(recruit => {
      val onlyAir     = recruit.canAttack && ! recruit.unitClass.attacksGround
      val onlyGround  = recruit.canAttack && ! recruit.unitClass.attacksAir
      val thisTarget  = if (onlyAir) targetAir else if (onlyGround) targetGround else target
      recruit.agent.intend(squad.client, new Intention {
        toTravel = Some(thisTarget.pixelCenter)
        toReturn = Some(home)
      })
    })
  }
  
  def defendHeart(center: Pixel) {
    val protectables = center.zone.units.filter(u => u.isOurs && u.unitClass.isBuilding && u.hitPoints < 300 && u.visibleToOpponents)
    val protectRange = ByOption.max(protectables.map(_.pixelDistanceCenter(center))).getOrElse(32.0 * 8.0)
    squad.units.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(center)
      toReturn = if (zone.bases.exists(_.owner.isUs)) Some(center) else None
      toLeash = Some(Leash(center, protectRange))
    }))
  }
  
  val wallConcaveWidthRadians : Double = Math.PI / 2.0
  val wallConcaveWidthPixels  : Double = 32.0 * 3.0
  
  def defendWall(walls: Seq[UnitInfo]) {
    val centroidSources = Seq(squad.enemies.toSeq.map(_.pixelCenter), zone.exit.map(_.pixelCenter).toSeq, Seq(SpecificPoints.middle))
    val enemyCentroid   = PurpleMath.centroid(centroidSources.find(_.nonEmpty).head)
    val wall            = walls.minBy(_.pixelDistanceTravelling(enemyCentroid))
    val concaveOrigin   = wall.pixelCenter.project(enemyCentroid, 96.0)
    val concaveAxis     = wall.pixelCenter.radiansTo(enemyCentroid)
    val concaveStart    = concaveOrigin.radiateRadians(concaveAxis + wallConcaveWidthRadians, wallConcaveWidthPixels)
    val concaveEnd      = concaveOrigin.radiateRadians(concaveAxis - wallConcaveWidthRadians, wallConcaveWidthPixels)
    defendLine(concaveStart, concaveEnd)
  }
  
  def defendChoke() {
    assignToFormation(zone.formation.form(squad.units.toSeq))
  }
  
  def defendLine(from: Pixel, to: Pixel) {
    val concaveMidpoint = from.midpoint(to)
    val concaveRadians  = from.radiansTo(to)
    val someDistance    = 96.0
    val rightAngle      = Math.PI / 2.0
    val origins         = Vector(
      concaveMidpoint.radiateRadians(concaveRadians + rightAngle, someDistance),
      concaveMidpoint.radiateRadians(concaveRadians - rightAngle, someDistance))
    val origin = origins.minBy(o =>
      if (With.geography.ourBases.isEmpty) 0.0
      else With.geography.ourBases.map(_.heart.pixelCenter.groundPixels(o)).min)
  
    concave(from, to, origin)
  }
  
  def concave(start: Pixel, end: Pixel, origin: Pixel) {
    val formation =
      Formations.concave(
        squad.units,
        start,
        end,
        origin)
  }

  def assignToFormation(formation: Formation): Unit = {
    squad.units.foreach(
      defender => {
        val spot = formation.placements.get(defender)
        defender.agent.intend(squad.client, new Intention {
          toForm    = spot
          toReturn  = spot
          toTravel  = spot.orElse(Some(zone.centroid.pixelCenter)) })
      })
  }
  
}
