package Micro.Squads.Goals

import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Formations.Formation
import Mathematics.Points.{Pixel, SpecificPoints}
import Micro.Agency.Intention
import Micro.Squads.Squad
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.ByOption
import Utilities.EnrichPixel.EnrichedPixelCollection

class SquadProtectZone(zone: Zone) extends SquadGoal {
  
  private var lastAction = "ProtectZone"
  override def toString: String = lastAction + zone
  
  def updateUnits() {
  
    lazy val base   = ByOption.minBy(zone.bases)(_.heart.tileDistanceManhattan(With.intelligence.mostBaselikeEnemyTile))
    lazy val choke  = zone.exit
    lazy val walls  = zone.units.toSeq.filter(u =>
      u.isOurs
      && u.unitClass.isStaticDefense
      && (squad.enemies.isEmpty || squad.enemies.exists(u.canAttack)))
    
    lazy val canHuntEnemies  = squad.enemies.exists(e => e.matchups.threatsInRange.isEmpty || e.matchups.vpfNetDiffused > 0.0)
    lazy val canDefendWall   = walls.nonEmpty
    lazy val canDefendHeart  = base.isDefined
    lazy val canDefendChoke  = choke.isDefined
    
    if (canHuntEnemies) {
      lastAction = "Hunt intruders in "
      huntEnemies(squad)
    }
    else if (canDefendWall) {
      lastAction = "Defend static defense in "
      defendWall(squad, walls)
    }
    else if (canDefendChoke) {
      lastAction = "Defend choke of "
      defendChoke(squad, choke.get)
    }
    else if (canDefendHeart) {
      lastAction = "Defend heart of "
      defendHeart(squad, base.map(_.heart.pixelCenter).getOrElse(zone.centroid.pixelCenter))
    }
  }
  
  def huntEnemies(squad: Squad) {
    val center = zone.centroid.pixelCenter
    val target = squad.enemies.minBy(_.pixelDistanceFast(center)).pixelCenter
    squad.recruits.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(target)
      toReturn = Some(center)
    }))
  }
  
  def defendWall(squad: Squad, walls: Seq[UnitInfo]) {
    val centroidSources = Seq(squad.enemies.toSeq.map(_.pixelCenter), zone.exit.map(_.centerPixel).toSeq, Seq(SpecificPoints.middle))
    val enemyCentroid   = centroidSources.find(_.nonEmpty).head.centroid
    val wall            = walls.minBy(_.pixelDistanceFast(enemyCentroid))
    val siegers         = squad.enemies.filter(e => walls.exists(e.canAttack))
    val maxSiegerRange  = ByOption.max(siegers.map(_.pixelRangeGround)).getOrElse(0.0)
    val maxWallRange    = walls.map(wall => ByOption.max(siegers.filter(wall.canAttack).map(sieger => wall.pixelRangeAgainstFromCenter(sieger))).getOrElse(0.0)).max
    
    squad.recruits.foreach(recruit => {
      val range           = recruit.effectiveRangePixels
      val margin          = wall.unitClass.radialHypotenuse + Math.max(32.0, maxWallRange - maxSiegerRange)
      val formationPoint  = wall.pixelCenter.project(enemyCentroid, margin)
      recruit.agent.intend(squad.client, new Intention {
        toTravel = Some(formationPoint)
        toReturn = Some(formationPoint)
      })
    })
  }
  
  def defendHeart(squad: Squad, center: Pixel) {
    squad.recruits.foreach(_.agent.intend(squad.client, new Intention {
      toTravel = Some(center)
      toReturn = if (zone.bases.exists(_.owner.isUs)) Some(center) else None
    }))
  }
  
  def defendChoke(squad: Squad, choke: Edge) {
    val formation =
      Formation.concave(
        squad.recruits,
        choke.sidePixels.head,
        choke.sidePixels.last,
        choke.zones
          .toList
          .sortBy(_.centroid.tileDistanceFast(With.geography.home))
          .sortBy( ! _.owner.isUs)
          .head
          .centroid
          .pixelCenter)
  
    squad.recruits.foreach(
      defender => {
        val spot = formation(defender)
        defender.agent.intend(squad.client, new Intention { toTravel = Some(spot) })
      })
  }
  
}
