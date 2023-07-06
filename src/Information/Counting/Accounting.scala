package Information.Counting

import Lifecycle.With
import Mathematics.Maff
import Performance.Cache
import Performance.Tasks.TimedTask
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?
import Utilities.Time.Forever

class Accounting extends TimedTask {

  lazy val mapBonus: Double = ?(With.strategy.isMoneyMap, 1.737, 1) // Empirical estimation
  
  lazy val workerIncomePerFrameMinerals : Double = 0.044 * mapBonus
  lazy val workerIncomePerFrameGas      : Double = 0.069

  // Should start at 50, of course,but this -30 offsets the effect of starting workers all being far from minerals
  var ourEstimatedTotalMinerals = 20.0
  var ourEstimatedTotalGas = 0.0

  var _lastFrame = 0
  override def onRun(budgetMs: Long): Unit = {
    val frameDiff = With.framesSince(_lastFrame)
    ourEstimatedTotalMinerals += frameDiff * ourIncomePerFrameMinerals
    ourEstimatedTotalGas      += frameDiff * ourIncomePerFrameGas
    _lastFrame = With.frame
  }

  def ourActualTotalMinerals    : Integer = With.self.gatheredMinerals
  def ourActualTotalGas         : Integer = With.self.gatheredGas

  def ourIncomePerFrameMinerals : Double = ourIncomePerFrameMineralsCache()
  def ourIncomePerFrameGas      : Double = ourIncomePerFrameGasCache()
  def ourIncomePerFrameGasMax   : Double = ourIncomePerFrameGasMaxCache() // Gas income if we dedicated all workers to it

  def ourActiveMiners   : Int = ourActiveMinersCache()
  def ourActiveDrillers : Int = ourActiveDrillersCache()

  def framesToMineMinerals(amount: Int): Int = Math.ceil(Maff.nanToN(amount / ourIncomePerFrameMinerals, Forever())).toInt
  def framesToMineGas     (amount: Int): Int = Math.ceil(Maff.nanToN(amount / ourIncomePerFrameGas, Forever())).toInt

  private val ourPatchesMineralsCache         = new Cache(() => ?(With.frame == 0, 9, With.geography.ourBases.map(_.minerals.size).sum))
  private val ourPatchesGasCache              = new Cache(() =>                       With.geography.ourBases.map(_.gas.count(g => g.complete && g.isOurs)).sum)
  private val ourActiveMinersCache            = new Cache(() => ?(With.frame == 0, 4, With.units.ours.count(u => isActivelyMining(u) && u.agent.toGather.exists(_.mineralsLeft > 0)))) // Subtract one to account for a lone miner+builder
  private val ourActiveDrillersCache          = new Cache(() =>                       With.units.ours.count(u => isActivelyMining(u) && u.agent.toGather.exists(_.gasLeft > 0)))
  private val ourIncomePerFrameMineralsCache  = new Cache(() => workerIncomePerFrameMinerals  * Math.min(2.0 * ourPatchesMineralsCache(), ourActiveMinersCache()))
  private val ourIncomePerFrameGasCache       = new Cache(() => workerIncomePerFrameGas       * Math.min(3.0 * ourPatchesGasCache(),      ourActiveDrillersCache()))
  private val ourIncomePerFrameGasMaxCache    = new Cache(() => workerIncomePerFrameGas       * Math.min(3.0 * ourPatchesGasCache(),      ourActiveDrillersCache() + ourActiveMinersCache()))

  private val activeMiningDistance = Math.pow(32.0 * 8.0, 2)
  private def isActivelyMining(unit: FriendlyUnitInfo): Boolean = unit.agent.toGather.exists(_.pixelDistanceSquared(unit) < activeMiningDistance)
}
