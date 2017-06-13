package Mathematics.Formations

import Mathematics.Pixels.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ArrayBuffer

object Concave {
  
  def generate(
    units       : Seq[UnitInfo],
    targetStart : Pixel,
    targetEnd   : Pixel,
    direction   : Pixel)
    : Seq[FormationParticipant] = {
  
    val targetCenter = targetStart.midpoint(targetEnd)
    
    val arc = new Arc(
      Math.PI, // Configurable
      targetCenter,
      targetCenter.radiansTo(direction)
    )
    
    val arcPlacement = new ArcPlacementState(arc, targetStart.pixelDistanceFast(targetEnd))
    
    val ranks = units
      .map(new FormationParticipant(_))
      .groupBy(_.range)
      .values
      .toList
      .sortBy(_.head.range)
    
    val output = new ArrayBuffer[FormationParticipant]
    
    ranks.foreach(rank => {
      arcPlacement.startRank(rank.head.range)
      rank.foreach(participant => {
        participant.pixelAfter = arcPlacement.reserveSpace(participant.unitClass.radialHypotenuse)
      })
    })
    
    output
  }
}






