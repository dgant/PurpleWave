package Planning.Plans

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.BuildIntent
import Planning.ResourceLocks.{LockCurrencyFor, LockUnits}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.GameTime
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.IsWorker

class PylonBlock extends Plan {

  val blockerLock: LockUnits = new LockUnits(this, IsWorker).setCounter(CountOne)
  val currencyLock = new LockCurrencyFor(this, Protoss.Pylon)

  var lastWorkerDeathFrame: Int = 0
  var lastWorker: Option[FriendlyUnitInfo] = None

  def baseIsEligible(base: Base): Boolean = (
    base.owner.isNeutral
    && ! base.zone.island
    && base.mineralsLeft > 7500
    && base.units.forall(u => ! u.isEnemy || ! u.likelyStillThere))

  override def onUpdate(): Unit = {
    if (lastWorker.exists(! _.alive)) {
      lastWorkerDeathFrame = With.frame
    }
    lastWorker = None

    if (With.framesSince(lastWorkerDeathFrame) < GameTime(1, 0)()) return

    val basesByProxmity = With.geography.bases
      .filterNot(_.zone.island)
      .sortBy(base =>
        Maff.min(With.geography.ourBases.map(_.heart.groundPixels(base.heart))).getOrElse(0.0)
        - Maff.min(With.geography.enemyBases.map(_.heart.groundPixels(base.heart))).getOrElse(0.0) )

    // Take the most distant bases
    // For maps with a middle base, ignore it
    val basesDistant = basesByProxmity.drop((basesByProxmity.size + 1) / 2).reverse

    val basesEligible = basesDistant.filter(base =>
      With.grids.scoutingPathsStartLocations.get(base.townHallTile.add(1, 1)) > 15
      && baseIsEligible(base)
      && base.naturalOf.forall(main =>
        baseIsEligible(main)
        && basesDistant.contains(main)))

    val basesUnblocked = basesEligible.filterNot(base => base.zone.units.exists(u => u.isFriendly && u.unitClass.isBuilding && u.tileArea.intersects(base.townHallArea)))
    basesUnblocked.headOption.foreach(baseToBlock => {
      blockerLock.acquire()
      blockerLock.units.headOption.foreach(blocker => {
        lastWorker = Some(blocker)
        val tileToBlock = baseToBlock.townHallTile.add(1, 1)
        val readyToBuild = blocker.pixelDistanceCenter(tileToBlock.center) < 256 && With.self.supplyUsed400 + 24 >= With.self.supplyTotal400
        if (readyToBuild) {
          currencyLock.acquire()
        }
        blocker.intend(this).addBuild(BuildIntent(Protoss.Pylon, tileToBlock, readyToBuild))
      })
    })
  }
}
