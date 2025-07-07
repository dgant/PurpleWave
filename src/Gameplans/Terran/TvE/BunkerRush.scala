package Gameplans.Terran.TvE

import Lifecycle.With
import Macro.Actions.MacroActions
import Placement.Access.PlaceLabels.DefendHall
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.{Protoss, Terran}
import Utilities.UnitFilters.IsWarrior

object BunkerRush extends MacroActions {
  def apply(): Unit = {
    With.scouting.enemyNatural
      .filter(_.townHall.isDefined)
      .foreach(natural => {
        if (enemies(IsWarrior, Protoss.PhotonCannon) == 0) {
          attack()
          get(1, Terran.Bunker, new PlacementQuery(Terran.Bunker).requireBase(natural).preferLabelYes(DefendHall))
        }
      })
  }
}
