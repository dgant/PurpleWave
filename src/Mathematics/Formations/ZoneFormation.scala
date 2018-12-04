package Mathematics.Formations

import Information.Geography.Types.Zone
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable.ArrayBuffer

class ZoneFormation(zone: Zone) extends FormationDesigner {

  def zoneScore(someZone: Zone, rangedUnits: Boolean): Double = {
    val exitWidth = Math.max(1.0, someZone.exit.map(_.radiusPixels).getOrElse(1.0))
    val altitudeDelta = someZone.centroid.altitudeBonus - zone.centroid.altitudeBonus
    val altitudeBonus = if (rangedUnits) {
      if (altitudeDelta < 0) 0.1 else if (altitudeDelta > 0) 10.0 else 1.0
    } else 1.0
    val output = altitudeBonus / exitWidth
    output
  }

  def form(units: Seq[FriendlyUnitInfo]): Formation = {
    // Find which zone we should actually defend
    // This is pretty hacky.
    val zoneCandidates = new ArrayBuffer[Zone]
    zoneCandidates += zone
    for (i <- 0 until 3) {
      val tail = zoneCandidates.last
      tail.exit.map(_.otherSideof(tail)).foreach(zoneCandidates.append(_))
    }
    zoneCandidates.maxBy(zoneScore(_, true)).formation.buildFormation(units)
  }

  def buildFormation(units: Seq[FriendlyUnitInfo]): Formation = {
    if (zone.exit.isEmpty) {
      // TODO: Island base?
      return Formation(Map.empty)
    }
    val exit = zone.exit.get

    val shouldArc = zone.centroid.altitudeBonus > exit.otherSideof(zone).centroid.altitudeBonus

    //if (shouldArc) {
      return Formation(Formations.concave(units, exit.sidePixels.head, exit.sidePixels.last, zone.centroid.pixelCenter))
    //}
  }
}
