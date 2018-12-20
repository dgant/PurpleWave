package Micro.Actions.Transportation.Caddy

import Lifecycle.With
import Micro.Actions.Combat.Targeting.Target
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Shuttling {

  // How far is the pickup radius for a Shuttle? Just a guess here
  val pickupRadius = 32
  val dropoffRadius = 32

  def canCaddy(transport: FriendlyUnitInfo): Boolean = {
    transport.isTransport
  }

  def canPickUp(transport: FriendlyUnitInfo, passenger: UnitInfo): Boolean = {
    passenger.unitClass.canBeTransported && passenger.unitClass.spaceRequired < transport.spaceRemaining
  }

  def targets(transport: FriendlyUnitInfo): Vector[FriendlyUnitInfo] = {
    transport.teammates.view.flatMap(_.friendly).filter(transport.canTransport).toVector
  }

  def mainPassenger(shuttle: FriendlyUnitInfo): Option[FriendlyUnitInfo] = {
    ByOption.maxBy(shuttle.loadedUnits)(p => p.subjectiveValue + p.frameDiscovered / 10000.0)
  }

  def passengerTarget(passenger: FriendlyUnitInfo): Option[UnitInfo] = {
    if (passenger.agent.toAttack.isDefined) return passenger.agent.toAttack

    Target.delegate(passenger)
    val output = passenger.agent.toAttack

    output
  }

  def pickupNeed(shuttle: FriendlyUnitInfo, hailer: FriendlyUnitInfo): Double = {
    val targetedByScarab = hailer.matchups.enemies.exists(r => r.is(Protoss.Reaver) && r.cooldownLeft > 0) &&
      With.units.inPixelRadius(hailer.pixelCenter, 32*7).exists(s => s.orderTarget.contains(hailer) && s.is(Protoss.Scarab))
    val endangered = hailer.matchups.framesOfSafety < shuttle.framesToTravelTo(hailer.pixelCenter) + 2 * shuttle.unitClass.framesToTurn180
    val sojourning = hailer.agent.toTravel.exists(_.pixelDistance(hailer.pixelCenter) > 32.0 * 20)
    (if(targetedByScarab) 100 else 1) + (if (endangered) 10 else 1) + (if (sojourning) 1 else 0)
  }
}
