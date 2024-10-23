package Gameplans.Protoss.PvT

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Macro.Requests.RequestUnit
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.Protoss

class PvTGasSteal extends GameplanImperative {

  override def executeBuild(): Unit = {
    if (With.placement.wall.isDefined) {
      // TODO: Build Pylon+Gate in wall
    }
    scoutOn(Protoss.Pylon)

    once(8,  Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)
    once(Protoss.Gateway)

    val scout = With.tactics.scoutWithWorkers.units.headOption
    val enemyMain = With.scouting.enemyMain
    if (scout.isDefined && enemyMain.exists(_.gas.forall(_.isNeutral))) {
      get(RequestUnit(Protoss.Assimilator, 1, placementQueryArg = Some(new PlacementQuery(Protoss.Assimilator).requireBase(enemyMain.get))))
    }

    once(13, Protoss.Probe)
    once(Protoss.Zealot)
    once(14, Protoss.Probe)
    once(2, Protoss.Pylon)
    once(15, Protoss.Probe)

    get(RequestUnit(Protoss.Assimilator, 1, placementQueryArg = Some(new PlacementQuery(Protoss.Assimilator).requireBase(With.geography.ourMain))))
    once(2, Protoss.Zealot)
    once(16, Protoss.Probe)
    once(Protoss.CyberneticsCore)
    once(17, Protoss.Probe)
    once(3, Protoss.Zealot)
    once(18, Protoss.Probe)
    once(3, Protoss.Pylon)
    once(20, Protoss.Probe)
    once(Protoss.DragoonRange)
    once(Protoss.Dragoon)
    once(4, Protoss.Gateway)
    once(22, Protoss.Probe)
    if (units(Protoss.Gateway) < 4) {
      gasWorkerCeiling(2)
    }
  }

  override def executeMain(): Unit = {

    attack()

    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)
    get(4, Protoss.Gateway)
    requireMiningBases(2)
    once(10, Protoss.Gateway)
    pumpGasPumps()
  }
}
