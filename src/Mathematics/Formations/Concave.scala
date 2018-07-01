package Mathematics.Formations

import Lifecycle.With
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
        .sortBy(radians => -  targetCenter.radiateRadians(radians, 128.0).pixelDistance(SpecificPoints.middle))
        .sortBy(radians =>    targetCenter.radiateRadians(radians, 128.0).zone != origin.zone)
        .head
    
    val arc = Arc(
      Math.PI, // Configurable.
      targetCenter,
      centerToArcRadians,
      targetStart.pixelDistance(targetEnd))
    
    val arcPlacement = new ArcPlacementState(arc)
    
    val walkers = units.filterNot(_.flying)
    val flyers  = units.filter(_.flying)
    val ranks   = walkers
      .map(new FormationSlot(_))
      .groupBy(_.idealDistancePixels)
      .values
      .toList
      .sortBy(_.head.idealDistancePixels)
    
    ranks.foreach(rank => {
      arcPlacement.startRank(rank.head.idealDistancePixels)
      rank.foreach(participant => {
        participant.pixelAfter = arcPlacement.reserveSpace(With.configuration.concaveMarginPixels + 2.0 * participant.unitClass.radialHypotenuse)
      })
    })
  
    val walkerSlots = ranks.flatten
    val flyerSlots  = flyers.map(flyer => new FormationSlot(flyer))
    flyerSlots.foreach(slot => slot.pixelAfter = targetCenter.radiateRadians(centerToArcRadians, slot.idealDistancePixels))
    
    val output = walkerSlots ++ flyerSlots
    output
  }
}






