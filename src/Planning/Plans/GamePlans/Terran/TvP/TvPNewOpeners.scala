package Planning.Plans.GamePlans.Terran.TvP

import Lifecycle.With
import Planning.Plans.GamePlans.All.GameplanImperative
import Planning.Plans.Macro.Automatic.Enemy
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Strategy
import Strategery.Strategies.Terran.{TvP1Rax, TvP2Fac, TvP1Fac}
import Utilities.?

abstract class TvPNewOpeners extends GameplanImperative {

  var openingComplete: Boolean = false

  def open(): Unit = {
    if (openingComplete) return
         if (doOpeningShortCircuits())  {}
    else if (TvP1Rax())               openRaxExpand()
    else if (TvP1Fac())    openFacExpand()
    else if (TvP2Fac())             open2Fac()
    else                                openFacExpand() // A reasonable default if something went wrong
  }

  def swapIn(opening: Strategy): Unit = {
    opening.swapIn()
    Seq(TvP1Rax, TvP1Fac, TvP2Fac).filterNot(opening==).foreach(_.swapOut())
  }

  def doOpeningShortCircuits(): Boolean = {
    if (With.fingerprints.proxyGateway()) {
      swapIn(TvP2Fac)
    } else if (With.fingerprints.gasSteal() && have(Terran.Barracks)) {
      swapIn(TvP1Rax)
    } else if (With.fingerprints.twoGate()) {
      swapIn(TvP2Fac)
    } else if (With.fingerprints.threeGateGoon() || With.fingerprints.fourGateGoon()) {
      swapIn(TvP2Fac)
    } else if (With.fingerprints.nexusFirst() && ! have(Terran.Refinery)) {
      // Until we have a proper bunker rush reaction
      open14CC()
      return true
    }
    false
  }

  def open14CC(): Unit = {
    status("Open14CC")
    get(9, Terran.SCV)
    get(Terran.SupplyDepot)
    get(14, Terran.SCV)
    requireBases(2)
    openingComplete ||= bases > 1
  }

  def openRaxExpand(): Unit = {
    status("OpenRax")
    get(9, Terran.SCV)
    get(Terran.SupplyDepot)
    get(11, Terran.SCV)
    get(Terran.Barracks)
    get(15, Terran.SCV)
    requireBases(2)
    get(Terran.Marine)
    if (enemies(Protoss.Zealot) > 0) {
      buildBunkersAtNatural(1)
    }
    get(2, Terran.SupplyDepot)
    get(Terran.Refinery)
    if ( ! enemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.forgeFe)) {
      pump(Terran.Marine, 4)
    }
    get(18, Terran.SCV)
    get(2, Terran.Factory)
    pump(Terran.Vulture)
    pump(Terran.Marine)
    openingComplete ||= bases > 1 && have(Terran.Factory)
  }

  def openFacExpand(): Unit = {
    status("Open1Fac")
    get(9, Terran.SCV)
    get(Terran.SupplyDepot)
    get(11, Terran.SCV)
    get(Terran.Barracks)
    get(12, Terran.SCV)
    get(Terran.Refinery)
    get(14, Terran.SCV)
    get(2, Terran.SupplyDepot)
    get(Terran.Marine)
    get(15, Terran.SCV)
    get(Terran.Factory)
    get(16, Terran.SCV)
    get(2, Terran.Marine)
    get(17, Terran.SCV)
    get(3, Terran.Marine)
    get(18, Terran.SCV)
    if (haveGasForUnit(Terran.Factory) && ! haveComplete(Terran.Factory)) {
      gasWorkerCeiling(1)
    }
    pumpSupply()
    pumpWorkers(oversaturate = true)
    pumpRatio(Terran.Vulture, 0, 8, Seq(Enemy(Protoss.Zealot, 1.0)))
    get(Terran.MachineShop)
    buildBunkersAtNatural(1)
    pump(Terran.SiegeTankUnsieged)
    pump(Terran.Vulture)
    get(?(units(Terran.Vulture) > 1, Terran.SpiderMinePlant, Terran.SiegeMode))
    pump(Terran.Marine, 4)
    requireBases(2)
    pump(Terran.Marine)
    get(Terran.SiegeMode)
    get(2, Terran.Factory)
    get(Terran.SpiderMinePlant)
    get(Terran.VultureSpeed)
    get(3, Terran.Factory)
    openingComplete ||= bases > 1
  }

  def open2Fac(): Unit = {
    status("Open2Fac")
    openingComplete ||= have(Terran.Factory) && unitsEver(Terran.Vulture) > 1
  }
}
