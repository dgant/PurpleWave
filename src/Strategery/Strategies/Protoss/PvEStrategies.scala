package Strategery.Strategies.Protoss

import Lifecycle.With
import Placement.Access.PlaceLabels
import Strategery.MapGroups
import Strategery.Strategies.Strategy
import bwapi.Race

class PvEStrategy extends Strategy {
  setOurRace(Race.Protoss)
}

object PvEProxy2Gate extends PvEStrategy {
  addSelectionRequirement(() => With.placement.get(PlaceLabels.Proxy2Gate).nonEmpty)
  blacklistOn(MapGroups.badForProxying: _*)
  blacklistVs(
    With.fingerprints.twoGate,
    With.fingerprints.proxyGateway,
    With.fingerprints.fourPool,
    With.fingerprints.ninePool)
  addChoiceVs(Race.Terran)(ProtossChoices.vsTerran_From_2Gate: _*)
  addChoiceVs(Race.Protoss)(ProtossChoices.vsProtoss_From_2Gate: _*)
  addChoiceVs(Race.Zerg)(ProtossChoices.vsZerg_From_2Gate: _*)
}

object PvECannonRushFE extends PvEStrategy {
  addSelectionRequirement(() => With.geography.mains.filterNot(_.isOurs).forall(b => With.placement.get(PlaceLabels.CannonRush).exists(_.tile.base.contains(b))))
  addChoiceVs(Race.Terran)(ProtossChoices.vsTerran_From_FFE: _*)
  addChoiceVs(Race.Protoss)(ProtossChoices.vsProtoss_From_FFE: _*)
  addChoiceVs(Race.Zerg)(ProtossChoices.vsZerg_From_FFE: _*)

}
object PvECannonRushMaximum extends PvEStrategy {
  addChoiceVs(Race.Terran)(ProtossChoices.vsTerran_From_FFE: _*)
  addChoiceVs(Race.Protoss)(ProtossChoices.vsProtoss_From_FFE: _*)
  addChoiceVs(Race.Zerg)(ProtossChoices.vsZerg_From_FFE: _*)
}
