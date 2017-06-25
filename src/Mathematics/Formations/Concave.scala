package Mathematics.Formations

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

object Concave {
  
  def generate(
    units       : Iterable[UnitInfo],
    targetStart : Pixel,
    targetEnd   : Pixel,
    origin      : Pixel)
    : Seq[FormationSlot] = {
  
    val targetCenter = targetStart.midpoint(targetEnd)
    val targetRadians = targetStart.radiansTo(targetEnd)
    val arcCenterRadians = List(
      targetRadians + Math.PI / 2,
      targetRadians - Math.PI / 2)
        .minBy(radians => targetCenter.radiateRadians(radians, 1.0).pixelDistanceFast(origin))
    
    val arc = Arc(
      Math.PI, // Configurable.
      targetCenter,
      arcCenterRadians,
      targetStart.pixelDistanceFast(targetEnd) / 2.0
    )
    
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






