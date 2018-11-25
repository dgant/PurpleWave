package Micro.Actions.Transportation.Caddy

import Lifecycle.With
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Caddying {

  def canCaddy(transport: FriendlyUnitInfo): Boolean = {
    transport.isTransport
  }

  def targets(transport: FriendlyUnitInfo): Vector[FriendlyUnitInfo] = {
    transport.teammates.toVector.flatMap(_.friendly).filter(transport.canTransport)
  }

  def pickupNeed(hailer: FriendlyUnitInfo): Double = {
    val targetedByScarab = hailer.matchups.enemies.exists(r => r.is(Protoss.Reaver) && r.cooldownLeft > 0) &&
      With.units.inPixelRadius(hailer.pixelCenter, 32*7).exists(s => s.orderTarget.contains(hailer) && s.is(Protoss.Scarab))
    val endangered = hailer.effectiveRangePixels > 32 * 5 && hailer.matchups.threatsInRange.exists(threat => hailer.effectiveRangePixels > threat.effectiveRangePixels)
    val sojourning = hailer.agent.toTravel.exists(_.pixelDistance(hailer.pixelCenter) > 32.0 * 20)

    (if(targetedByScarab) 100 else 1) + (if (endangered) 10 else 1) + (if (sojourning) 1 else 0)
  }
}
