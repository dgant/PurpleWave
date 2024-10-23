package Gameplans.All

import Gameplans.Protoss.ProtossStandardGameplan
import Gameplans.Terran.TerranStandardGameplan
import Gameplans.Zerg.ZergStandardGameplan
import Planning.Plans.SwitchOurRace

class StandardGameplan extends SwitchOurRace(
  whenTerran  = new TerranStandardGameplan,
  whenProtoss = new ProtossStandardGameplan,
  whenZerg    = new ZergStandardGameplan)