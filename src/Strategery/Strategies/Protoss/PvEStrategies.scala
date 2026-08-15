package Strategery.Strategies.Protoss

import Lifecycle.With
import Placement.Access.PlaceLabels.Proxy2Gate
import Strategery.MapGroups
import Strategery.Strategies.Strategy
import bwapi.Race

class PvEStrategy extends Strategy {
  setOurRace(Race.Protoss)
}

object PvEProxy2Gate extends PvEStrategy {
  addSelectionRequirement(() => With.placement.get(Proxy2Gate).nonEmpty)
  blacklistOn(MapGroups.badForProxying: _*)
  blacklistVs(
    With.fingerprints.twoGate,
    With.fingerprints.proxyGateway,
    With.fingerprints.fourPool,
    With.fingerprints.ninePool)
  addChoice(ProtossChoices.vsTerran_From_2Gate: _*)
  addChoice(ProtossChoices.vsProtoss_From_2Gate: _*)
  addChoice(ProtossChoices.vsZerg_From_2Gate: _*)
}

object PvECannonRushFE extends PvEStrategy {
  addChoice(ProtossChoices.vsTerran_From_FFE: _*)
  addChoice(ProtossChoices.vsProtoss_From_FFE: _*)
  addChoice(ProtossChoices.vsZerg_From_FFE: _*)

}
object PvECannonRushMaximum extends PvEStrategy {
  addChoice(ProtossChoices.vsTerran_From_FFE: _*)
  addChoice(ProtossChoices.vsProtoss_From_FFE: _*)
  addChoice(ProtossChoices.vsZerg_From_FFE: _*)
}
