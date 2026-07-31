package Gameplans.Protoss.PvE

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.PlaceLabels.Proxy2Gate
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvEProxy2Gate => PvEProxy2GateStrategy}

class PvEProxy2Gate extends GameplanImperative {

  doAutosupply = false

  override def activated: Boolean = PvEProxy2GateStrategy()

  override def executeBuild(): Unit = {
    once(8, Protoss.Probe)
    val enemyMains = With.geography.mains.filterNot(_.isOurMain)
    get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon)
      .requireLabelYes(Proxy2Gate)
      .requireZone(With.geography.zones.filter(_.heart.groundTiles(With.geography.home) > 40): _*)
      .preferBase(enemyMains: _*)
      .preferZone(With.geography.mains.filterNot(_.isOurMain).map(_.zone): _*))
    once(9, Protoss.Probe)
    get(2, Protoss.Gateway)
  }

  override def executeMain(): Unit = {
    pump(Protoss.Zealot)
    attack()
  }
}
