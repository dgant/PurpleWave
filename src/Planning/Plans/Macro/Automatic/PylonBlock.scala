package Planning.Plans.Macro.Automatic

import Information.Geography.Types.Base
import Lifecycle.With
import Micro.Agency.Intention
import Planning.Plan
import Planning.ResourceLocks.{LockCurrency, LockUnits}
import Planning.UnitCounters.UnitCountOne
import Planning.UnitMatchers.UnitMatchWorkers
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.{ByOption, GameTime}

class PylonBlock extends Plan {

  val blockerLock = new LockUnits {
    unitMatcher.set(UnitMatchWorkers)
    unitCounter.set(UnitCountOne)
  }

  val currencyLock = new LockCurrency {
    minerals = 100
  }

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
        ByOption.min(With.geography.ourBases.map(_.heart.groundPixels(base.heart))).getOrElse(0.0)
        - ByOption.min(With.geography.enemyBases.map(_.heart.groundPixels(base.heart))).getOrElse(0.0) )

    // Take the most distant bases
    // For maps with a middle base, ignore it
    val basesDistant = basesByProxmity.drop((basesByProxmity.size + 1) / 2).reverse

    val basesEligible = basesDistant.filter(base =>
      With.grids.scoutingPathsStartLocations.get(base.townHallTile.add(1, 1)) > 15
      && baseIsEligible(base)
      && base.isNaturalOf.forall(main =>
        baseIsEligible(main)
        && basesDistant.contains(main)))

    val basesUnblocked = basesEligible.filterNot(base => base.zone.units.exists(u => u.isFriendly && u.unitClass.isBuilding && u.tileArea.intersects(base.townHallArea)))
    basesUnblocked.headOption.foreach(baseToBlock => {
      blockerLock.acquire(this)
      blockerLock.units.headOption.foreach(blocker => {
        lastWorker = Some(blocker)
        val tileToBlock = baseToBlock.townHallTile.add(1, 1)
        var intent = new Intention {
          toTravel = Some(tileToBlock.pixelCenter)
          canFlee = true
          canFight = false
          canFocus = true
        }
        if (blocker.pixelDistanceCenter(tileToBlock.pixelCenter) < 256 && With.self.supplyUsed + 24 >= With.self.supplyTotal) {
          currencyLock.acquire(this)
          intent = new Intention {
            toBuild = Some(Protoss.Pylon)
            toBuildTile = Some(tileToBlock)
          }
        }
        blocker.agent.intend(this, intent)
      })
    })
  }
}
