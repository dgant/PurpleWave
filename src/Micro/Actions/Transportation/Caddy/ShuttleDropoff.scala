package Micro.Actions.Transportation.Caddy

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.Micro.ShowUnitsFriendly
import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Commands.Move
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShuttleDropoff extends Action {

  override def allowed(shuttle: FriendlyUnitInfo): Boolean = {
    shuttle.is(Protoss.Shuttle) && shuttle.loadedUnits.nonEmpty
  }

  override protected def perform(shuttle: FriendlyUnitInfo): Unit = {
    val passenger = Shuttling.mainPassenger(shuttle)
    val destination = passenger.map(Shuttling.passengerDestination).getOrElse(shuttle.agent.destination)
    shuttle.agent.toTravel = Some(destination)

    // Get off Mr. Shuttle's wild ride
    if (shuttle.agent.toTravel.forall(p => shuttle.pixelDistanceCenter(p) < Shuttling.dropoffRadius)) {
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
