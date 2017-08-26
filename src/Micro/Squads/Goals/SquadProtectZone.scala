package Micro.Squads.Goals

import Information.Geography.Types.{Edge, Zone}
import Lifecycle.With
import Mathematics.Formations.Formation
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Micro.Squads.Squad
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.EnrichPixel.EnrichedPixelCollection

class SquadProtectZone(zone: Zone) extends SquadGoal {
  
  private var lastAction = "ProtectZone"
  override def toString: String = lastAction + zone.name
  
  def update(squad: Squad) {
  
    lazy val base   = zone.bases.find(_.owner.isUs)
    lazy val choke  = zone.exit
    lazy val walls  = With.units.ours.toSeq.filter(u => u.unitClass.isStaticDefense && u.zone == zone)
    
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
  
  def defendWall(squad: Squad, walls: Seq[FriendlyUnitInfo]) {
    val enemyCentroid = squad.enemies.map(_.pixelCenter).centroid
    val wall = walls.minBy(_.pixelDistanceFast(enemyCentroid))
    
    squad.recruits.foreach(recruit => {
      val formationPoint = wall.pixelCenter.project(enemyCentroid, 32.0 * 7.0 - recruit.effectiveRangePixels)
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
