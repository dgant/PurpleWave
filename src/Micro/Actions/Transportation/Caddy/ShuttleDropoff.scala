package Micro.Actions.Transportation.Caddy

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.Micro.ShowUnitsFriendly
import Lifecycle.With
import Mathematics.Shapes.Spiral
import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object ShuttleDropoff extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = {
    shuttle.is(Protoss.Shuttle) && shuttle.loadedUnits.nonEmpty
  }

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    lazy val passenger = ByOption.maxBy(shuttle.loadedUnits)(p => p.subjectiveValue + p.frameDiscovered / 10000.0)
    lazy val target = passenger.flatMap(_.agent.toAttack)
    lazy val targetSnipeStart = target.map(_.pixelCenter.project(shuttle.pixelCenter, passenger.get.effectiveRangePixels).tileIncluding)
    lazy val targetSnipeTiles = targetSnipeStart.map(tile =>
      Spiral
        .points(7)
        .map(tile.add)
        .filter(tile => tile.valid && With.grids.walkable.get(tile))
        .filter(With.grids.walkable.get))
      .getOrElse(Seq.empty)
    lazy val targetSnipeFinal = ByOption.minBy(targetSnipeTiles)(tile => {
      val enemyRange = Math.max(1.0, With.grids.enemyRange.get(tile))
      val distanceTarget = target.get.pixelDistanceCenter(tile.pixelCenter)
      val distanceIdeal = passenger.get.effectiveRangePixels + passenger.get.topSpeed * passenger.get.cooldownMaxAgainst(target.get)
      val distanceOff = Math.max(1.0, distanceIdeal - distanceTarget)
      val distanceShuttle = Math.max(16.0, shuttle.pixelDistanceCenter(tile.pixelCenter))
      enemyRange * distanceOff * distanceShuttle
    }).map(_.pixelCenter)

    shuttle.agent.toTravel = targetSnipeFinal.orElse(shuttle.agent.toTravel)

    // Get off Mr. Shuttle's wild ride
    if (shuttle.agent.toTravel.forall(p => shuttle.pixelDistanceCenter(p) < 32)) {
      shuttle.loadedUnits.foreach(passenger => With.commander.unload(shuttle, passenger))
    }

    if (ShowUnitsFriendly.inUse && With.visualization.map) {
      shuttle.agent.toTravel.foreach(p => {
        DrawMap.line(p, shuttle.pixelCenter, Colors.MediumGreen)
        DrawMap.circle(p, shuttle.unitClass.width / 2, Colors.MediumGreen)
      })
    }

    Move.delegate(shuttle)
  }
}
