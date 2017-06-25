package Mathematics.Formations

import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitInfo.UnitInfo

object Concave {
  
  def generate(
    units       : Iterable[UnitInfo],
    targetStart : Pixel,
    targetEnd   : Pixel,
    origin      : Pixel)
    : Seq[FormationSlot] = {
  
    val targetCenter  = targetStart.midpoint(targetEnd)
    val targetRadians = targetStart.radiansTo(targetEnd)
    val centerToArcRadians = List(
      targetRadians - Math.PI / 2.0,
      targetRadians + Math.PI / 2.0)
        .sortBy(radians => - targetCenter.radiateRadians(radians, 128.0).pixelDistanceFast(SpecificPoints.middle))
        .sortBy(radians => targetCenter.radiateRadians(radians, 128.0).zone != origin.zone)
        .head
    
    val arc = Arc(
      Math.PI, // Configurable.
      targetCenter,
      centerToArcRadians,
      targetStart.pixelDistanceFast(targetEnd) / 2.0)
    
    val arcPlacement = new ArcPlacementState(arc, targetStart.pixelDistanceFast(targetEnd))
    
    val ranks = units
      .map(new FormationSlot(_))
      .groupBy(_.idealDistance)
      .values
      .toList
      .sortBy(_.head.idealDistance)
    
    ranks.foreach(rank => {
      arcPlacement.startRank(rank.head.idealDistance)
      rank.foreach(participant => {
        participant.pixelAfter = arcPlacement.reserveSpace(2.0 + 2.0 * participant.unitClass.radialHypotenuse)
      })
    })
  
    ranks.flatten
  }
}






