package Gameplans.Protoss.PvE

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Mathematics.Points.Tile
import Placement.Access.PlaceLabels.{Proxy2Gate, Proxy4Gate}
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Strategery.Strategies.Protoss.{PvEProxy2Gate => PvEProxy2GateStrategy}

class PvEProxy2Gate extends GameplanImperative {

  doAutosupply = false

  override def activated: Boolean = PvEProxy2GateStrategy()

  override def executeBuild(): Unit = {
    once(8, Protoss.Probe)
    get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon)
      .preferLabelYes(Proxy2Gate, Proxy4Gate)
      .preferZone(With.geography.mains.filterNot(_.isOurs).map(_.zone): _*))
    once(9, Protoss.Probe)
    get(2, Protoss.Gateway)
  }

  override def executeMain(): Unit = {
    pump(Protoss.Zealot)
    attack()
  }
}
