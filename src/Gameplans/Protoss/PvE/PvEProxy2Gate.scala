package Gameplans.Protoss.PvE

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.PlaceLabels._
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvEProxy2Gate
import Utilities.?
import Utilities.Time.Minutes

class PvEProxy2Gate extends GameplanImperative {

  override def activated: Boolean = PvEProxy2Gate()
  override def completed: Boolean = With.frame > Minutes(8)()

  override def executeBuild(): Unit = {
    scoutOn(Protoss.Gateway, quantity = 2)

    once(8, Protoss.Probe)
    val enemyMains = With.geography.mains.filterNot(_.isOurMain)
    if ( ! haveEver(Protoss.Pylon)) {
      get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon)
        .requireLabelYes(Proxy2Gate)
        .requireZone(With.geography.zones.filter(_.heart.groundTiles(With.geography.home) > 40): _*)
        .preferBase(enemyMains: _*)
        .preferZone(With.geography.mains.filterNot(_.isOurMain).map(_.zone): _*))
    }
    once(9, Protoss.Probe)
    get(2, Protoss.Gateway)
    once(11, Protoss.Probe)
    once(1, Protoss.Zealot)
    once(2, Protoss.Zealot)
    get(2, Protoss.Pylon)
    once(3, Protoss.Zealot)
    once(12, Protoss.Probe)
  }

  doAutosupply = false

  override def doWorkers(): Unit = {}

  override def executeMain(): Unit = {
    val strength = With.units.playerOwned
      .filter(u => u.complete && u.unitClass.isWarrior)
      .map(u => u.unitClass.mineralPrice * u.totalHealth / u.unitClass.maxTotalHealth.toDouble * ?(u.isFriendly, 1.5, -1))
      .sum
    if (strength > 0) {
      attack()
    }

    val unpoweredGateways = With.units.ours.count(u => Protoss.Gateway(u) && ! u.powered)

    gasWorkerCeiling(?(gas >= 100 && With.self.gatheredGas > 250, 2, 3))
    gasLimitCeiling(250)
    requireMiningBases(1)
    if (have(Protoss.CyberneticsCore)) {
      autosupply()
      pumpWorkers()
      get(Protoss.DragoonRange)
      pump(Protoss.Dragoon)
      get(3 + unpoweredGateways, Protoss.Gateway)
      pump(Protoss.Zealot)
    } else {
      if (With.enemy.isZerg) {
        autosupply()
        pump(Protoss.Zealot)
        pumpWorkers()
        get(6, Protoss.Gateway)
      } else {
        once(5, Protoss.Zealot)
        /*
        val batteryBases = With.scouting.enemyMain.flatMap(_.natural).toSeq
        ?(
          enemyIsProtoss,
          get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon)
            .requireBase(batteryBases: _*)
            .preferLabelYes(DefendGround, DefendEntrance, GroundProduction)
            .preferTile(batteryBases.flatMap(_.entranceNow).map(_.pixelCenter.tile): _*)),
          get(3, Protoss.Pylon))
          *
         */
        once(6, Protoss.Zealot)
        once(13, Protoss.Probe)
        once(7, Protoss.Zealot)
        get(1, Protoss.Assimilator, new PlacementQuery(Protoss.Assimilator).requireBase(With.geography.ourMain))
        once(14, Protoss.Probe)
        get(Protoss.CyberneticsCore)
        once(15, Protoss.Probe)
        get(3, Protoss.Gateway)
        once(16, Protoss.Probe)
        pump(Protoss.Zealot)
        pumpWorkers()
          /*
          get(2, Protoss.ShieldBattery, new PlacementQuery(Protoss.ShieldBattery)
            .requireBase(batteryBases: _*)
            .preferLabelYes(DefendGround, DefendEntrance, GroundProduction)
            .preferTile(batteryBases.flatMap(_.entranceNow).map(_.pixelCenter.tile): _*)),
          */
      }
    }
    tryBuildInWall(1, Protoss.Pylon)
    tryBuildInWall(1, Protoss.Forge)
    tryBuildInWall(2, Protoss.PhotonCannon)
    requireMiningBases(2)
  }

  def slushOrPylons(): Unit = {
    if (With.tactics.workerScout.scoutingAbandoned || ! enemyIsProtoss) {
      autosupply()
      return
    }
    if (With.tactics.workerScout.scouts.exists(_.base.exists(_.isEnemy))) {
      microSlush(100)
    } else {
      autosupply()
    }
  }
}
