package Micro.Actions.Transportation.Caddy

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.Shapes.Spiral
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
    passenger.agent.toAttack.orElse(ByOption.minBy(passenger.matchups.targets)(_.pixelDistanceCenter(passenger)))
  }

  def passengerDestination(passenger: FriendlyUnitInfo): Pixel = {
    val target = passengerTarget(passenger)
    if (target.isEmpty) return passenger.agent.destination

    val targetSnipeStart = target.get.pixelCenter.project(passenger.pixelCenter, passenger.effectiveRangePixels).tileIncluding
    val targetSnipeTiles =
      Spiral
        .points(9)
        .map(targetSnipeStart.add)
        .filter(tile => tile.valid && With.grids.walkable.get(tile))
    val targetSnipeFinal = ByOption.minBy(targetSnipeTiles)(tile => {
      val enemyRange = 1.0 + Math.max(0.0, With.grids.enemyRange.get(tile) - With.grids.enemyRange.addedRange)
      val enemyFactor = enemyRange * enemyRange
      val distanceTarget = target.get.pixelCenter.groundPixels(tile.pixelCenter)
      val distanceIdeal = passenger.effectiveRangePixels //+ passenger.topSpeed * passenger.cooldownMaxAgainst(target.get)
      val distanceOff = 1.0 + Math.max(0.0, distanceIdeal - distanceTarget)
      val distanceFactor = distanceOff * distanceOff
      val visionFactor = if (With.grids.enemyVision.isSet(tile)) 3 else 2
      enemyFactor * distanceFactor * visionFactor
    }).map(_.pixelCenter)
    targetSnipeFinal.getOrElse(passenger.agent.destination)
  }

  def pickupNeed(shuttle: FriendlyUnitInfo, hailer: FriendlyUnitInfo): Double = {
    val targetedByScarab = hailer.matchups.enemies.exists(r => r.is(Protoss.Reaver) && r.cooldownLeft > 0) &&
      With.units.inPixelRadius(hailer.pixelCenter, 32*7).exists(s => s.orderTarget.contains(hailer) && s.is(Protoss.Scarab))
    val endangered = hailer.matchups.framesOfSafety < shuttle.framesToTravelTo(hailer.pixelCenter) + 2 * shuttle.unitClass.framesToTurn180
    val sojourning = hailer.agent.toTravel.exists(_.pixelDistance(hailer.pixelCenter) > 32.0 * 20)
    (if(targetedByScarab) 100 else 1) + (if (endangered) 10 else 1) + (if (sojourning) 1 else 0)
  }
}
