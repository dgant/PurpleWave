package Planning.Plans.Macro.Automatic

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Planning.Plan
import Planning.ResourceLocks.LockCurrencyForUnit
import ProxyBwapi.Races.{Terran, Zerg}
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.Race

class StealGas extends Plan {

  val currencyLock = new LockCurrencyForUnit(if (With.self.raceCurrent == Race.Zerg) Zerg.Extractor else Terran.Refinery)

  override def onUpdate(): Unit = {
    if (With.frame > GameTime(6, 0)()) return

    val scouts = With.units.ours.filter(_.agent.canScout).map(_.asInstanceOf[UnitInfo]).toSet
    val base = With.geography.enemyBases.find(base => base.owner.isEnemy && scouts.exists(_.base == base))
    val gas = base.map(_.gas.filter(_.player.isNeutral)).getOrElse(Iterable.empty)

    if (gas.size != 1) return

    With.blackboard.stealGas.set(true)
    currencyLock.framesPreordered = scouts.map(_.framesToTravelTo(gas.head.pixelCenter)).min * 4 / 5
    currencyLock.acquire(this)
  }
}
