package Macro

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo


import scala.collection.mutable

/**
  * Tracks mineral patches which can be used to accelerate a worker on the way to a different mineral patch.
  * Flying/floating units like workers decelerate as they approach their order target.
  * By ordering them to a distant mineral that causes them to approach
  */
trait AccelerantMinerals {

  def getAccelerantMineral(mineral: UnitInfo): Option[UnitInfo] = accelerantMinerals.get(mineral)

  private var acceleratorsInitialized: Boolean = false
  val accelerantMinerals = new mutable.HashMap[UnitInfo, UnitInfo]()

  protected def initializeAccelerators(): Unit = {
    acceleratorsInitialized = true
    With.geography.bases.foreach(initializeBaseAccelerators)
  }

  private def initializeBaseAccelerators(base: Base): Unit = {
    // Two potential kinds of accelerators:
    // - Back minerals, for front minerals
    // - Inner minerals, for outer minerals (to create straighter paths)
    // Not sure how to do the second one yet so this will just do the first

    val hallStart = base.townHallArea.startPixel
    val hallEnd = base.townHallArea.endPixel

    val minerals = base.minerals.map(m => (m, Maff.broodWarDistanceBox(m.topLeft, m.bottomRight, hallStart, hallEnd))).sortBy(_._2)
    var i = 0
    while (i < minerals.length) {
      val mineral = minerals(i)
      val acceleratorCandidates = minerals.drop(i).filter(_._2 > 24 + mineral._2).filter(_._1.pixelDistanceEdge(mineral._1) < 40)
      val accelerator = Maff.minBy(acceleratorCandidates)(_._1.pixelDistanceEdge(mineral._1))
      accelerator.foreach(a => accelerantMinerals(mineral._1) = a._1)
      i += 1
    }
  }
}
