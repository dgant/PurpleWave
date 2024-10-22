package Planning.Plans.Gameplans.All

import Planning.Plans.Compound.SwitchOurRace
import Planning.Plans.Gameplans.Protoss.ProtossStandardGameplan
import Planning.Plans.Gameplans.Terran.TerranStandardGameplan
import Planning.Plans.Gameplans.Zerg.ZergStandardGameplan

class StandardGameplan extends SwitchOurRace(
  whenTerran  = new TerranStandardGameplan,
  whenProtoss = new ProtossStandardGameplan,
  whenZerg    = new ZergStandardGameplan)