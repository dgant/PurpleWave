package Gameplans.Terran.TvE

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Macro.Actions.Enemy
import Placement.Access.PlaceLabels
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.{?, SwapIf}

class TvE3Fac extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(9, Terran.SCV)
    get(Terran.SupplyDepot)
    once(11, Terran.SCV)
    get(Terran.Barracks)
    once(12, Terran.SCV)
    pumpGasPumps()

    scoutAt(13)

    once(14, Terran.SCV)
    once(Terran.Marine)

    if (With.fingerprints.fourPool() || With.fingerprints.proxyGateway() || With.fingerprints.proxyRax()) {
      buildBunkersAtMain(1, PlaceLabels.DefendHall)
      pump(Terran.Marine, 4)
      pumpWorkers(oversaturate = false)
    }
    once(15, Terran.SCV)
    get(2, Terran.SupplyDepot)
    once(16, Terran.SCV)
    once(Terran.Factory)
    once(18, Terran.SCV)
    once(2, Terran.Factory)
  }

  override def doWorkers(): Unit = {
    pumpWorkers(oversaturate = false, maximumTotal = 23)
  }

  override def executeMain(): Unit = {
    once(Terran.Vulture)

    SwapIf(
      With.fingerprints.twoGate(),
      get(Terran.MachineShop),
      once(3, Terran.Vulture))

    if (enemyMutalisksLikely || enemyHasShown(Protoss.Scout, Protoss.Carrier, Terran.Wraith)) {
      get(Terran.Armory)
      get(Terran.GoliathAirRange)
    }
    if (enemyLurkersLikely || enemyDarkTemplarLikely || enemyHasTech(Terran.WraithCloak)) {
      get(Terran.Academy)
      get(Terran.Comsat)
    }
    if (enemyHasShown(Terran.Bunker, Protoss.PhotonCannon, Zerg.SunkenColony)) {
      get(Terran.SiegeMode)
      get(2, Terran.MachineShop)
      once(2, Terran.SiegeTankUnsieged)
    }

    if (enemyStrategy(With.fingerprints.fiveRax, With.fingerprints.eightRax, With.fingerprints.twoGate, With.fingerprints.ninePool) && ! haveComplete(Terran.Vulture) && unitsComplete(Terran.Factory) < 2) {
      pump(Terran.Marine, 6)
    }

    SwapIf(
      With.fingerprints.oneGateCore(),
      get(Terran.VultureSpeed),
      get(Terran.SpiderMinePlant))

    pumpRatio(Terran.Goliath, ?(enemyMutalisksLikely, 3, 1), 18, Seq(Enemy(Zerg.Mutalisk, 2.0), Enemy(Protoss.Scout, 2.0), Enemy(Terran.Wraith, 1.0), Enemy(Protoss.Carrier, 4.0)))
    if (techStarted(Terran.SiegeMode)) {
      pump(Terran.SiegeTankUnsieged)
    }
    pump(Terran.Vulture)
    get(3, Terran.Factory)
    get(Terran.SiegeMode)
    attack()
  }
}
