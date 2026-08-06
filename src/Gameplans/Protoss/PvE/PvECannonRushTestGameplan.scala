package Gameplans.Protoss.PvE

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.PlaceLabels.CannonRush
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass

class PvECannonRushTestGameplan extends GameplanImperative {

  doAutosupply = false

  private def query(building: UnitClass, tile: Mathematics.Points.Tile): PlacementQuery =
    new PlacementQuery(building)
      .requireBuilding(building)
      .requireLabelYes(CannonRush)
      .requireTile(tile)

  override def executeBuild(): Unit = {
    once(8, Protoss.Probe)
    get(Protoss.Pylon)
    get(Protoss.Forge)
    With.placement.cannonRushes.foreach(rush =>
      get(Protoss.Pylon, query(Protoss.Pylon, rush.pylons.head)))
    With.placement.cannonRushes.foreach(rush => {
      val firstPylon = rush.pylons.head
      val firstPylonComplete = With.units.ours.exists(unit =>
        Protoss.Pylon(unit) && unit.complete && unit.tileTopLeft == firstPylon)
      if (firstPylonComplete) {
        get(Protoss.PhotonCannon, query(Protoss.PhotonCannon, rush.cannon))
      }
      val cannonStarted = With.units.ours.exists(unit =>
        Protoss.PhotonCannon(unit) && unit.tileTopLeft == rush.cannon)
      if (cannonStarted) {
        rush.pylons.drop(1).foreach(pylon => get(Protoss.Pylon, query(Protoss.Pylon, pylon)))
      }
    })
    once(10, Protoss.Probe)
  }

  override def executeMain(): Unit = {}
}
