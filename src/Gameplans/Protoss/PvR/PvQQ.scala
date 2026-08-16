package Gameplans.Protoss.PvR

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.PlacementQuery
import ProxyBwapi.Races.Protoss

class PvQQ extends GameplanImperative {

  object NF
  object GFE
  object FFE
  var plan: Option[Any] = None

  override def executeBuild(): Unit = {
    scoutOn(Protoss.Pylon)
    once(8, Protoss.Probe)
    tryBuildInWall(1, Protoss.Pylon)
    once(13, Protoss.Probe)

    if (plan.isEmpty) {
      if (enemyIsTerran) {
        plan = Some(NF)
      } else if (enemyIsProtoss) {
        plan = Some(GFE)
      } else if (enemyIsZerg) {
        plan = Some(FFE)
      } else if (have(Protoss.Forge)) {
        plan = Some(FFE)
      }
    }
    if (plan.contains(NF)) {
      requireMiningBases(2)
      once(14, Protoss.Probe)
      tryBuildInWall(1, Protoss.Gateway)
    } else if (plan.contains(FFE)) {
      tryBuildInWall(1, Protoss.Forge)
      if (
        (enemyIsTerran && ! With.fingerprints.proxyRax()) ||
        With.fingerprints.nexusFirst() ||
        With.fingerprints.hatchFirst()) {
        requireMiningBases(2)
        once(15, Protoss.Probe)
        tryBuildInWall(1, Protoss.Gateway)
        once(17, Protoss.Probe)
        tryBuildInWall(2, Protoss.PhotonCannon)
        get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon).requireBase(With.geography.ourMain))
        once(Protoss.Zealot)
        get(Protoss.Assimilator)
        once(18, Protoss.Probe)
        get(Protoss.CyberneticsCore)
        once(19, Protoss.Probe)
        once(2, Protoss.Zealot)
      } else {
        tryBuildInWall(2, Protoss.PhotonCannon)
        requireMiningBases(2)
        once(14, Protoss.Probe)
        tryBuildInWall(1, Protoss.Gateway)
        once(17, Protoss.Probe)
        get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon).requireBase(With.geography.ourMain))
        once(18, Protoss.Probe)
        get(Protoss.Assimilator)
        once(19, Protoss.Probe)
        get(Protoss.CyberneticsCore)
      }
    } else if (plan.contains(GFE)) {
      tryBuildInWall(1, Protoss.Gateway)
      once(13, Protoss.Probe)
      once(Protoss.Zealot)
      once(15, Protoss.Probe)
      get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon).requireBase(With.geography.ourMain))
      once(2, Protoss.Zealot)
      requireMiningBases(2)
      once(15, Protoss.Probe)
      once(2, Protoss.Zealot)
      get(Protoss.Assimilator)
      once(16, Protoss.Probe)
      get(Protoss.CyberneticsCore)
      once(17, Protoss.Probe)
      once(3, Protoss.Zealot)
    } else {
      get(Protoss.Forge)
      once(14, Protoss.Probe)
    }
  }

  def executeMain(): Unit = {
    With.blackboard.darkTemplarSquad.set(false)
    attack(false)

    if ( ! safeDefending) {
      buildInWall(6, Protoss.PhotonCannon)
    }

    get(Protoss.ArbiterEnergy)
    get(1, Protoss.Arbiter)
    get(1, Protoss.DarkTemplar)
    get(1, Protoss.Scout)
    get(1, Protoss.Corsair)
    get(1, Protoss.HighTemplar)
    pump(Protoss.Reaver)
    pump(Protoss.Dragoon)
    pump(Protoss.Zealot)
    get(Protoss.Gateway)
    get(Protoss.Assimilator)
    get(Protoss.CyberneticsCore)
    get(Protoss.DragoonRange)
    get(3, Protoss.Gateway)
    get(Protoss.CitadelOfAdun)
    get(Protoss.TemplarArchives)
    upgradeContinuously(Protoss.GroundDamage) && upgradeContinuously(Protoss.GroundArmor)
    get(Protoss.ZealotSpeed)
    get(Protoss.RoboticsFacility)
    get(Protoss.RoboticsSupportBay)
    pumpGasPumps()
    get(Protoss.Stargate)
    get(Protoss.ArbiterTribunal)
    get(6, Protoss.Gateway)
    pumpGasPumps()
    get(8, Protoss.Gateway)
  }
}
