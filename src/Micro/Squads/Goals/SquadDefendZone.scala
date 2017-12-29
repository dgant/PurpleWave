package Micro.Squads.Goals

import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Formations.Formation
import Mathematics.Points.{Pixel, SpecificPoints}
import Micro.Agency.Intention
import Performance.Cache
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption
import Utilities.EnrichPixel.EnrichedPixelCollection

class SquadDefendZone(zone: Zone) extends SquadGoal {
  
  private var lastAction = "ProtectZone"
  override def toString: String = lastAction + zone
  
  override def acceptsHelp: Boolean = false
  
  def updateUnits() {
    lazy val unitWidth  = 2.0 * squad.recruits.map(_.unitClass.radialHypotenuse).sum
    lazy val base       = ByOption.minBy(zone.bases)(_.heart.tileDistanceManhattan(With.intelligence.mostBaselikeEnemyTile))
    lazy val choke      = zone.exit
    lazy val walls      = zone.units.toSeq.filter(u =>
      u.isOurs
      && u.unitClass.isStaticDefense
      && (squad.enemies.isEmpty || squad.enemies.exists(u.canAttack)))
    
    lazy val canHuntEnemies  = huntableEnemies().nonEmpty
    lazy val canDefendWall   = walls.nonEmpty
    lazy val canDefendChoke  = choke.isDefined && ( ! With.enemies.exists(_.isZerg) || choke.get.radiusPixels * 2.0 - 32.0 < unitWidth)
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
    squad.enemies.filter(enemy => enemy.zone == zone && ! enemy.unitClass.isWorker)
  })
  
  def huntEnemies() {
    lazy val center        = zone.bases.find(_.owner.isUs).map(_.heart.pixelCenter).getOrElse(zone.centroid.pixelCenter)
    lazy val target        = huntableEnemies().minBy(_.pixelDistanceFast(center))
    lazy val targetAir     = ByOption.minBy(huntableEnemies().filter   (_.flying))(_.pixelDistanceFast(center)).getOrElse(target)
    lazy val targetGround  = ByOption.minBy(huntableEnemies().filterNot(_.flying))(_.pixelDistanceFast(center)).getOrElse(target)
    squad.recruits.foreach(recruit => {
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
    squad.recruits.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(center)
      toReturn = if (zone.bases.exists(_.owner.isUs)) Some(center) else None
    }))
  }
  
  val wallConcaveWidthRadians : Double = Math.PI / 2.0
  val wallConcaveWidthPixels  : Double = 32.0 * 3.0
  
  def defendWall(walls: Seq[UnitInfo]) {
    val centroidSources = Seq(squad.enemies.toSeq.map(_.pixelCenter), zone.exit.map(_.centerPixel).toSeq, Seq(SpecificPoints.middle))
    val enemyCentroid   = centroidSources.find(_.nonEmpty).head.centroid
    val wall            = walls.minBy(_.pixelDistanceFast(enemyCentroid))
    val concaveOrigin   = wall.pixelCenter.project(enemyCentroid, 96.0)
    val concaveAxis     = wall.pixelCenter.radiansTo(enemyCentroid)
    val concaveStart    = concaveOrigin.radiateRadians(concaveAxis + wallConcaveWidthRadians, wallConcaveWidthPixels)
    val concaveEnd      = concaveOrigin.radiateRadians(concaveAxis - wallConcaveWidthRadians, wallConcaveWidthPixels)
    concave(concaveStart, concaveEnd, concaveOrigin)
  }
  
  def defendChoke(choke: Edge) {
    val concaveStart  = choke.sidePixels.head
    val concaveEnd    = choke.sidePixels.last
    val concaveOrigin = choke.zones.toList
      .sortBy(_.centroid.tileDistanceFast(With.geography.home))
      .sortBy( ! _.owner.isUs)
      .head
      .centroid
      .pixelCenter
    
    concave(concaveStart, concaveEnd, concaveOrigin)
  }
  
  def concave(start: Pixel, end: Pixel, origin: Pixel) {
    val formation =
      Formation.concave(
        squad.recruits,
        start,
        end,
        origin)
  
    squad.recruits.foreach(
      defender => {
        val spot = formation(defender)
        defender.agent.intend(squad.client, new Intention {
          toForm    = Some(spot)
          toReturn  = Some(spot)
          toTravel  = Some(spot) })
      })
  }
  
}
