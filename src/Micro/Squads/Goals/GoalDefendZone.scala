package Micro.Squads.Goals

import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Formations.Formation
import Mathematics.Points.{Pixel, SpecificPoints}
import Micro.Agency.Intention
import Performance.Cache
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption
import Utilities.EnrichPixel.EnrichedPixelCollection

class GoalDefendZone(var zone: Zone) extends GoalBasic {
  
  private var lastAction = "Defend "
  
  override def toString: String = lastAction + zone
  
  override def acceptsHelp: Boolean = false
  
  override def run() {
    lazy val base       = ByOption.minBy(zone.bases)(_.heart.tileDistanceManhattan(With.intelligence.mostBaselikeEnemyTile))
    lazy val choke      = zone.exit
    lazy val walls      = zone.units.toSeq.filter(u =>
      u.isOurs
      && u.unitClass.isStaticDefense
      && (squad.enemies.isEmpty || squad.enemies.exists(u.canAttack)))
    
    lazy val canHuntEnemies  = huntableEnemies().nonEmpty
    lazy val canDefendWall   = walls.nonEmpty
    lazy val canDefendChoke  = choke.isDefined && ( ! With.enemies.exists(_.isZerg) || squad.units.size >= 6)
    lazy val canDefendHeart  = base.isDefined
    
    if (canHuntEnemies) {
      lastAction = "Scour "
      huntEnemies()
    }
    else if (canDefendWall) {
      lastAction = "Protect wall of "
      defendWall(walls)
    }
    else if (canDefendChoke) {
      lastAction = "Protect choke of "
      defendChoke(choke.get)
    }
    else if (canDefendHeart) {
      lastAction = "Protect heart of "
      defendHeart(base.map(_.heart.pixelCenter).getOrElse(zone.centroid.pixelCenter))
    }
  }
  
  private val huntableEnemies = new Cache(() => {
    squad.enemies.filter(enemy =>
      enemy.visible
      && (enemy.zone == zone || enemy.zone.edges.exists(edge => edge.zones.contains(zone)))
      && ! enemy.is(Zerg.Drone))
  })
  
  def huntEnemies() {
    lazy val center        = zone.bases.find(_.owner.isUs).map(_.heart.pixelCenter).getOrElse(zone.centroid.pixelCenter)
    lazy val target        = huntableEnemies().minBy(_.pixelDistanceCenter(center))
    lazy val targetAir     = ByOption.minBy(huntableEnemies().filter   (_.flying))(_.pixelDistanceCenter(center)).getOrElse(target)
    lazy val targetGround  = ByOption.minBy(huntableEnemies().filterNot(_.flying))(_.pixelDistanceCenter(center)).getOrElse(target)
    squad.units.foreach(recruit => {
      val onlyAir     = recruit.canAttack && ! recruit.unitClass.attacksGround
      val onlyGround  = recruit.canAttack && ! recruit.unitClass.attacksAir
      val thisTarget  = if (onlyAir) targetAir else if (onlyGround) targetGround else target
      recruit.agent.intend(squad.client, new Intention {
        toTravel = Some(thisTarget.pixelCenter)
        toReturn = Some(center)
      })
    })
  }
  
  def defendHeart(center: Pixel) {
    squad.units.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(center)
      toReturn = if (zone.bases.exists(_.owner.isUs)) Some(center) else None
    }))
  }
  
  val wallConcaveWidthRadians : Double = Math.PI / 2.0
  val wallConcaveWidthPixels  : Double = 32.0 * 3.0
  
  def defendWall(walls: Seq[UnitInfo]) {
    val centroidSources = Seq(squad.enemies.toSeq.map(_.pixelCenter), zone.exit.map(_.pixelCenter).toSeq, Seq(SpecificPoints.middle))
    val enemyCentroid   = centroidSources.find(_.nonEmpty).head.centroid
    val wall            = walls.minBy(_.pixelDistanceTravelling(enemyCentroid))
    val concaveOrigin   = wall.pixelCenter.project(enemyCentroid, 96.0)
    val concaveAxis     = wall.pixelCenter.radiansTo(enemyCentroid)
    val concaveStart    = concaveOrigin.radiateRadians(concaveAxis + wallConcaveWidthRadians, wallConcaveWidthPixels)
    val concaveEnd      = concaveOrigin.radiateRadians(concaveAxis - wallConcaveWidthRadians, wallConcaveWidthPixels)
    defendLine(concaveStart, concaveEnd)
  }
  
  def defendChoke(choke: Edge) {
    defendLine(choke.sidePixels.head, choke.sidePixels.last)
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
      Formation.concave(
        squad.units,
        start,
        end,
        origin)
  
    squad.units.foreach(
      defender => {
        val spot = formation(defender)
        defender.agent.intend(squad.client, new Intention {
          toForm    = Some(spot)
          toReturn  = Some(spot)
          toTravel  = Some(spot) })
      })
  }
  
}
