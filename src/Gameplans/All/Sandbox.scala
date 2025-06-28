package Gameplans.All

import Placement.Access.PlaceLabels
import ProxyBwapi.Races.Terran

class Sandbox extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(9, Terran.SCV)
    get(Terran.SupplyDepot)
    once(11, Terran.SCV)
    get(Terran.Barracks)
    once(13, Terran.SCV)
    get(2, Terran.Barracks)
    once(15, Terran.SCV)
    get(2, Terran.SupplyDepot)
    once(Terran.Marine)
  }
  override def executeMain(): Unit = {
    pump(Terran.Marine)
    buildBunkersAtMain(1, PlaceLabels.DefendHall)
    buildBunkersAtMain(1, PlaceLabels.DefendEntrance)
    buildBunkersAtNatural(1, PlaceLabels.DefendHall)
    buildBunkersAtNatural(1, PlaceLabels.DefendEntrance)
    get(Terran.EngineeringBay)
    buildTurretsAtMain(4, PlaceLabels.DefendHall)
    buildTurretsAtMain(1, PlaceLabels.DefendEntrance)
    buildTurretsAtNatural(4, PlaceLabels.DefendHall)
    buildTurretsAtNatural(1, PlaceLabels.DefendEntrance)
  }
}
