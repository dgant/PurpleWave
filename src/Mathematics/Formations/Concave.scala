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
    
    val arc = Arc(
      Math.PI, // Configurable
      targetCenter,
      targetCenter.radiansTo(origin),
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
        participant.pixelAfter = arcPlacement.reserveSpace(2 * participant.unitClass.radialHypotenuse)
      })
    })
  
    ranks.flatten
  }
}






