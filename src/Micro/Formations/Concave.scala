package Micro.Formations

import Mathematics.Points.Pixel
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable.ArrayBuffer

object Concave {
  
  def generate(
    units       : Iterable[UnitInfo],
    targetStart : Pixel,
    targetEnd   : Pixel,
    origin      : Pixel)
    : Seq[FormationSlot] = {
  
    val targetCenter = targetStart.midpoint(targetEnd)
    
    val arc = new Arc(
      Math.PI, // Configurable
      targetCenter,
      targetCenter.radiansTo(origin)
    )
    
    val arcPlacement = new ArcPlacementState(arc, targetStart.pixelDistanceFast(targetEnd))
    
    val ranks = units
      .map(new FormationSlot(_))
      .groupBy(_.idealDistance)
      .values
      .toList
      .sortBy(_.head.idealDistance)
    
    val output = new ArrayBuffer[FormationSlot]
    
    ranks.foreach(rank => {
      arcPlacement.startRank(rank.head.idealDistance)
      rank.foreach(participant => {
        participant.pixelAfter = arcPlacement.reserveSpace(participant.unitClass.radialHypotenuse)
      })
    })
    
    output
  }
}






